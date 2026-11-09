package app.coshell.transactions.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.transactions.DeleteTransactionWithUndo
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.Currencies
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.navigation.TransactionEditDestination
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption
import app.coshell.transactions.edit.TransactionEditUiState.ToAccountError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * New or existing transaction. Validation lives in [validateForm], category choices in
 * [withCategoryOptions]; this class only loads data, keeps the state and saves.
 */
@HiltViewModel
class TransactionEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
    private val saveTransaction: SaveTransactionUseCase,
    private val ids: IdGenerator,
    private val moneyFormat: MoneyFormat,
    private val clock: Clock,
    private val timeZone: TimeZone,
    private val rates: ExchangeRateRepository,
    private val deleteWithUndo: DeleteTransactionWithUndo,
) : ViewModel() {

    private val transactionId: String? = savedStateHandle[TransactionEditDestination.ARG_TRANSACTION_ID]
    private val initialType: TransactionType = savedStateHandle.get<String>(TransactionEditDestination.ARG_TYPE)
        ?.let { name -> TransactionType.entries.firstOrNull { it.name == name } }
        ?: TransactionType.EXPENSE

    private var existing: Transaction? = null
    private var accountsById: Map<String, Account> = emptyMap()
    private var allCategories: List<Category> = emptyList()
    /** Known rates for the "debited" suggestion; `null` until loaded. */
    private var converter: CurrencyConverter? = null

    private val _state = MutableStateFlow(TransactionEditUiState(isLoading = true, isNew = transactionId == null))
    val state: StateFlow<TransactionEditUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch {
            rates.observeRates().collect { list ->
                converter = CurrencyConverter(RateBook(list))
                if (!_state.value.isLoading) updateForm { it }
            }
        }
    }

    // region Loading

    private suspend fun load() {
        val transaction = transactionId?.let { transactions.getTransaction(it) }
        existing = transaction
        val used = setOfNotNull(transaction?.accountId, transaction?.toAccountId)
        // Archived accounts and categories are hidden unless the edited transaction uses them.
        val accountList = accounts.observeAccounts(includeArchived = true).first()
            .filter { !it.archived || it.id in used }
        accountsById = accountList.associateBy { it.id }
        allCategories = categories.observeCategories(includeArchived = true).first()
            .filter { !it.archived || it.id == transaction?.categoryId }

        val today = clock.now().toLocalDateTime(timeZone).date
        val recent = transactions.observeTransactions().first()
        val base = TransactionEditUiState(
            accounts = accountList.map { it.toOption() },
            today = today,
            receiptCurrencies = receiptCurrencySuggestions(recent, accountList),
        )
        val loaded = if (transaction == null) newForm(base, accountList, recent) else formOf(transaction, base)
        _state.value = loaded.withCategoryOptions(allCategories)
    }

    /** A new transaction starts on today and on the account used last. */
    private fun newForm(
        base: TransactionEditUiState,
        accountList: List<Account>,
        recent: List<Transaction>,
    ): TransactionEditUiState {
        val lastAccount = recent.firstOrNull()?.accountId
        return base.copy(
            isNew = true,
            type = initialType,
            accountId = lastAccount?.takeIf { it in accountsById } ?: accountList.firstOrNull()?.id,
            date = base.today,
        )
    }

    private fun formOf(transaction: Transaction, base: TransactionEditUiState) = base.copy(
        isNew = false,
        type = transaction.type,
        amount = moneyFormat.formatForEditing(transaction.amount),
        toAmount = transaction.toAmount?.let { moneyFormat.formatForEditing(it) }.orEmpty(),
        accountId = transaction.accountId,
        toAccountId = transaction.toAccountId,
        categoryId = transaction.categoryId,
        date = transaction.date.toLocalDateTime(timeZone).date,
        note = transaction.note,
        receiptEnabled = transaction.originalAmount != null,
        receiptCurrency = transaction.originalAmount?.currency?.value.orEmpty(),
        receiptAmount = transaction.originalAmount?.let { moneyFormat.formatForEditing(it) }.orEmpty(),
    )

    // endregion

    // region Form input

    fun onTypeChange(type: TransactionType) = _state.update {
        it.copy(type = type, categoryError = false, toAccountError = null).withCategoryOptions(allCategories)
    }

    fun onAmountChange(value: String) = _state.update {
        it.copy(amount = value, amountError = null, amountSuggested = false, suggestedRateDate = null)
    }

    fun onToAmountChange(value: String) = _state.update { it.copy(toAmount = value, toAmountError = null) }

    fun onAccountSelect(id: String) = updateForm {
        it.copy(accountId = id, accountError = false, amountError = null, toAccountError = null, receiptCurrencyError = null)
    }

    fun onToAccountSelect(id: String) = _state.update { it.copy(toAccountId = id, toAccountError = null) }

    /** Selecting a folder selects the folder itself and opens its subcategories. */
    fun onCategorySelect(id: String) = _state.update {
        it.copy(categoryId = id, categoryError = false).withCategoryOptions(allCategories)
    }

    fun onDateSelect(date: LocalDate) = updateForm { it.copy(date = date) }

    /** From Material's DatePicker, which works in UTC midnight millis. */
    fun onDatePicked(millisUtc: Long) =
        onDateSelect(Instant.fromEpochMilliseconds(millisUtc).toLocalDateTime(TimeZone.UTC).date)

    fun onNoteChange(value: String) = _state.update { it.copy(note = value) }

    /** Turning the receipt on preselects the last receipt currency that differs from the account's. */
    fun onReceiptToggle(enabled: Boolean) = updateForm {
        val currency = it.receiptCurrency.ifEmpty { it.receiptCurrencyChoices.firstOrNull().orEmpty() }
        it.copy(receiptEnabled = enabled, receiptCurrency = currency, receiptCurrencyError = null, receiptAmountError = null)
    }

    fun onReceiptCurrencyChange(value: String) = updateForm {
        it.copy(receiptCurrency = value.trim().uppercase(), receiptCurrencyError = null, receiptAmountError = null)
    }

    fun onReceiptAmountChange(value: String) = updateForm { it.copy(receiptAmount = value, receiptAmountError = null) }

    /** Changes that affect the receipt conversion also refresh the suggested debited amount. */
    private fun updateForm(change: (TransactionEditUiState) -> TransactionEditUiState) =
        _state.update { change(it).withDebitSuggestion(converter, moneyFormat) }

    // endregion

    // region Save and delete

    fun save() {
        val current = _state.value
        if (current.isLoading || current.isDone) return
        val form = when (val result = validateForm(current, accountsById)) {
            is FormResult.Invalid -> {
                _state.update { it.withErrors(result.errors) }
                return
            }
            is FormResult.Valid -> result.form
        }
        val transaction = buildTransaction(existing, form, dateWithTime(existing?.date ?: clock.now(), current.date, timeZone), ids)
        viewModelScope.launch {
            when (val result = saveTransaction(transaction)) {
                SaveTransactionUseCase.Result.Saved -> _state.update { it.copy(isDone = true) }
                is SaveTransactionUseCase.Result.Invalid -> _state.update { it.withError(result.reason) }
            }
        }
    }

    fun delete() {
        val transaction = existing ?: return
        viewModelScope.launch {
            deleteWithUndo(transaction.id)
            _state.update { it.copy(isDone = true) }
        }
    }

    // endregion

    private fun TransactionEditUiState.withErrors(errors: FormErrors) = copy(
        accountError = errors.accountError,
        amountError = errors.amountError,
        toAccountError = errors.toAccountError,
        toAmountError = errors.toAmountError,
        categoryError = errors.categoryError,
        receiptCurrencyError = errors.receiptCurrencyError,
        receiptAmountError = errors.receiptAmountError,
    )

    /** Use case refusals that the form could not foresee (e.g. an account deleted on the other phone). */
    private fun TransactionEditUiState.withError(reason: SaveTransactionUseCase.Reason) = when (reason) {
        SaveTransactionUseCase.Reason.ACCOUNT_NOT_FOUND,
        SaveTransactionUseCase.Reason.CURRENCY_MISMATCH,
        -> copy(accountError = true)
        SaveTransactionUseCase.Reason.TARGET_ACCOUNT_NOT_FOUND,
        SaveTransactionUseCase.Reason.TARGET_CURRENCY_MISMATCH,
        -> copy(toAccountError = ToAccountError.REQUIRED)
        SaveTransactionUseCase.Reason.CATEGORY_NOT_FOUND,
        SaveTransactionUseCase.Reason.CATEGORY_TYPE_MISMATCH,
        -> copy(categoryError = true, categoryId = null)
        SaveTransactionUseCase.Reason.ORIGINAL_CURRENCY_INVALID ->
            copy(receiptCurrencyError = TransactionEditUiState.ReceiptCurrencyError.SAME_AS_ACCOUNT)
    }

    private fun Account.toOption() =
        AccountOption(id, name, currency.value, Currencies.info(currency).displaySymbol)
}

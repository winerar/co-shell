package app.coshell.transactions.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.transactions.DeleteTransactionWithUndo
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.tagIds
import app.coshell.core.model.TransactionType
import app.coshell.navigation.TransactionEditDestination
import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.model.Currencies
import app.coshell.core.model.CurrencyCode
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption
import app.coshell.transactions.edit.TransactionEditUiState.EntryStep
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget
import app.coshell.transactions.edit.TransactionEditUiState.ToAccountError
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    private val entryTemplates: EntryTemplates,
    private val entryTags: EntryTags,
) : ViewModel() {

    /** The edited operation; `null` for a new one. «Дублировать» in the header copies it. */
    val transactionId: String? = savedStateHandle[TransactionEditDestination.ARG_TRANSACTION_ID]
    private val initialType: TransactionType = savedStateHandle.get<String>(TransactionEditDestination.ARG_TYPE)
        ?.let { name -> TransactionType.entries.firstOrNull { it.name == name } }
        ?: TransactionType.EXPENSE

    private val copyFromId: String? = savedStateHandle[TransactionEditDestination.ARG_COPY_FROM]

    /** Template from the route («Все шаблоны» → tap); applied once after the first load. */
    private var pendingTemplateId: String? = savedStateHandle[TransactionEditDestination.ARG_TEMPLATE_ID]
    /** The template the form was filled from; its usage is counted when the operation is saved. */
    private var templateId: String? = null

    private var existing: Transaction? = null
    /** Labels of the edited or copied operation: references the form does not change keep their form. */
    private var sourceLabels: List<String> = emptyList()
    private var accountsById: Map<String, Account> = emptyMap()
    private var allCategories: List<Category> = emptyList()
    private var recentCategoryIds: List<String> = emptyList()
    /** Known rates for the "debited" suggestion; `null` until loaded. */
    private var converter: CurrencyConverter? = null

    private val _state = MutableStateFlow(TransactionEditUiState(isLoading = true, isNew = transactionId == null))
    val state: StateFlow<TransactionEditUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch {
            // Parsing and indexing every known rate is heavy: off the main thread (#258).
            rates.observeRates().map { list -> CurrencyConverter(RateBook.of(list)) }.offMain().collect { built ->
                converter = built
                if (!_state.value.isLoading) updateForm { it }
            }
        }
        viewModelScope.launch {
            entryTags.observe().offMain().collect { choices -> _state.update { it.copy(tags = choices) } }
        }
    }

    // region Loading

    private suspend fun load() {
        val transaction = transactionId?.let { transactions.getTransaction(it) }
        existing = transaction
        val template = copyFromId?.let { transactions.getTransaction(it) }
        sourceLabels = (transaction ?: template)?.labels.orEmpty()
        val used = setOfNotNull(transaction?.accountId, transaction?.toAccountId, template?.accountId, template?.toAccountId)
        // Archived accounts and categories are hidden unless the edited transaction uses them.
        val withBalances = accounts.observeAccountsWithBalance(includeArchived = true).first()
            .filter { !it.account.archived || it.account.id in used }
        val accountList = withBalances.map { it.account }
        val balances = withBalances.associate { it.account.id to moneyFormat.format(it.balance) }
        accountsById = accountList.associateBy { it.id }
        allCategories = categories.observeCategories(includeArchived = true).first()
            .filter { !it.archived || it.id == transaction?.categoryId }

        val today = clock.now().toLocalDateTime(timeZone).date
        // The newest operations are enough for the last account, the category order and receipt currencies (#149).
        val recent = transactions.observeTransactions(TransactionFilter(limit = RECENT_OPERATIONS)).first()
        recentCategoryIds = recent.mapNotNull { it.categoryId }.distinct()
        val base = TransactionEditUiState(
            accounts = accountList.map { it.toOption(balances[it.id].orEmpty()) },
            recentAccountIds = recent.map { it.accountId }.distinct().filter { it in accountsById }.take(RECENT_ACCOUNTS),
            today = today,
            receiptCurrencies = receiptCurrencySuggestions(recent, accountList),
            tags = _state.value.tags,
        )
        val loaded = when {
            transaction != null -> formOf(transaction, base)
            // «Дублировать»: the same operation as a new one today, straight on the amount.
            template != null -> formOf(template, base).copy(isNew = true, isCopy = true, date = base.today)
            else -> newForm(base, accountList, recent)
        }
        _state.value = loaded.withCategoryOptions(allCategories, recentCategoryIds)
        pendingTemplateId?.let { applyTemplate(it) }
        pendingTemplateId = null
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
            // Owner 2026-11-09: the account is chosen first, even when the last one is preselected.
            step = TransactionEditUiState.EntryStep.ACCOUNT,
        )
    }

    private fun formOf(transaction: Transaction, base: TransactionEditUiState): TransactionEditUiState {
        // An income in an expense category is a refund (#208): edited as an expense with «↩ Возврат» on.
        val refund = transaction.type == TransactionType.INCOME &&
            allCategories.any { it.id == transaction.categoryId && it.type == CategoryType.EXPENSE }
        return formFields(transaction, base).copy(type = if (refund) TransactionType.EXPENSE else transaction.type, refund = refund)
    }

    private fun formFields(transaction: Transaction, base: TransactionEditUiState) = base.copy(
        isNew = false,
        step = TransactionEditUiState.EntryStep.DETAILS,
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
        tagIds = transaction.tagIds,
    )

    // endregion

    // region Templates

    /** A chip of the strip: the same sheet, filled from the template. */
    fun onTemplatePick(id: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch { applyTemplate(id) }
    }

    /** «✕» on the template pill: back to an empty new operation. */
    fun onTemplateClear() {
        templateId = null
        viewModelScope.launch { load() }
    }

    private suspend fun applyTemplate(id: String) {
        val (draft, name) = entryTemplates.draft(id, clock.now(), accountsById, allCategories) ?: return
        templateId = id
        _state.update { it.filledFrom(draft, name, moneyFormat).withCategoryOptions(allCategories, recentCategoryIds) }
    }

    /** «Сохранить как шаблон» for the saved operation (not for unsaved edits in the form). */
    fun saveAsTemplate(name: String, rememberAmount: Boolean) {
        val transaction = existing ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val saved = entryTemplates.saveAs(transaction, name, rememberAmount)
            _state.update { it.copy(savedTemplateName = name.trim().takeIf { saved }, templateSaveFailed = !saved) }
        }
    }

    // endregion

    // region Form input

    fun onTypeChange(type: TransactionType) = _state.update {
        it.copy(type = type, refund = false, categoryError = false, toAccountError = null).withCategoryOptions(allCategories, recentCategoryIds)
    }

    /** «↩ Возврат» chip of an expense (#208): the same category, the sign flips. */
    fun onRefundToggle(enabled: Boolean) = _state.update { it.copy(refund = enabled && it.type == TransactionType.EXPENSE) }

    fun onAmountChange(value: String) = _state.update {
        it.copy(amount = value, amountError = null, amountSuggested = false, suggestedRateDate = null)
    }

    fun onToAmountChange(value: String) = _state.update { it.copy(toAmount = value, toAmountError = null) }

    /** On the account step a tap also moves on: to the target account of a transfer, else to the amount. */
    fun onAccountSelect(id: String) = updateForm {
        it.copy(
            accountId = id,
            accountError = false,
            amountError = null,
            toAccountError = null,
            receiptCurrencyError = null,
            step = it.step.afterAccount(it.isTransfer),
            keypadTarget = KeypadTarget.AMOUNT,
        )
    }

    fun onToAccountSelect(id: String) = _state.update {
        val step = if (it.step == EntryStep.TO_ACCOUNT) EntryStep.DETAILS else it.step
        it.copy(toAccountId = id, toAccountError = null, step = step)
    }

    fun onStepChange(step: EntryStep) = _state.update { it.copy(step = step) }

    fun onKeypadTarget(target: KeypadTarget) = _state.update { it.copy(keypadTarget = target) }

    /** A keypad key types into [TransactionEditUiState.keypadTarget]; «Готово» saves. */
    fun onKey(key: KeypadKey) {
        val current = _state.value
        if (key == KeypadKey.Done) return save()
        when (current.keypadTarget) {
            KeypadTarget.AMOUNT ->
                onAmountChange(AmountKeys.apply(current.amount, key, current.account?.fractionDigits ?: 2))
            KeypadTarget.TO_AMOUNT ->
                onToAmountChange(AmountKeys.apply(current.toAmount, key, current.toAccount?.fractionDigits ?: 2))
            KeypadTarget.RECEIPT ->
                onReceiptAmountChange(AmountKeys.apply(current.receiptAmount, key, fractionDigitsOf(current.receiptCurrency)))
        }
    }

    /** Selecting a folder selects the folder itself and opens its subcategories. */
    fun onCategorySelect(id: String) = _state.update {
        it.copy(categoryId = id, categoryError = false).withCategoryOptions(allCategories, recentCategoryIds)
    }

    fun onDateSelect(date: LocalDate) = updateForm { it.copy(date = date) }

    /** From Material's DatePicker, which works in UTC midnight millis. */
    fun onDatePicked(millisUtc: Long) =
        onDateSelect(Instant.fromEpochMilliseconds(millisUtc).toLocalDateTime(TimeZone.UTC).date)

    fun onNoteChange(value: String) = _state.update { it.copy(note = value) }

    /** A pill in the row or a row of the «Теги» sheet: on ↔ off. */
    fun onTagToggle(id: String) = _state.update {
        it.copy(tagIds = if (id in it.tagIds) it.tagIds - id else it.tagIds + id)
    }

    /** «Новый тег»: created (or found by name) and chosen at once. */
    fun onTagCreate(name: String, color: Long?) {
        viewModelScope.launch {
            val id = entryTags.create(name, color) ?: return@launch
            _state.update { if (id in it.tagIds) it else it.copy(tagIds = it.tagIds + id) }
        }
    }

    /** Turning the receipt on preselects the last receipt currency that differs from the account's. */
    fun onReceiptToggle(enabled: Boolean) = updateForm {
        val currency = it.receiptCurrency.ifEmpty { it.receiptCurrencyChoices.firstOrNull().orEmpty() }
        it.copy(
            receiptEnabled = enabled,
            receiptCurrency = currency,
            receiptCurrencyError = null,
            receiptAmountError = null,
            // The receipt amount is what one reads off the paper first; «списано» follows by the rate.
            keypadTarget = if (enabled) KeypadTarget.RECEIPT else KeypadTarget.AMOUNT,
        )
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
        val date = dateWithTime(existing?.date ?: clock.now(), current.date, timeZone)
        val transaction = buildTransaction(existing, form, date, ids, sourceLabels)
        viewModelScope.launch {
            when (val result = saveTransaction(transaction)) {
                SaveTransactionUseCase.Result.Saved -> {
                    templateId?.let { entryTemplates.used(it) }
                    _state.update { it.copy(isDone = true) }
                }
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

    private fun Account.toOption(balance: String) =
        AccountOption(id, name, currency.value, type, color, balance, Currencies.info(currency).fractionDigits)

    private fun fractionDigitsOf(code: String): Int =
        CurrencyCode.parseOrNull(code)?.let { Currencies.info(it).fractionDigits } ?: 2

    private companion object {
        const val RECENT_ACCOUNTS = 4
        const val RECENT_OPERATIONS = 500
    }
}

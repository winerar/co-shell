package app.coshell.accounts.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.accounts.edit.AccountEditUiState.BalanceError
import app.coshell.accounts.edit.AccountEditUiState.CurrencyError
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.AdjustBalanceUseCase
import app.coshell.core.domain.DeleteAccountUseCase
import app.coshell.core.domain.SaveAccountUseCase
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.MoneyParser
import app.coshell.navigation.AccountEditDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccountEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accounts: AccountRepository,
    private val saveAccount: SaveAccountUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val ids: IdGenerator,
    private val moneyFormat: MoneyFormat,
    private val adjustBalance: AdjustBalanceUseCase,
    private val otherMembers: OtherMembers = OtherMembers { flowOf(emptyMap()) },
    private val reportCurrencies: ReportCurrencySettings = DefaultReportCurrencies,
) : ViewModel() {

    private val accountId: String? = savedStateHandle[AccountEditDestination.ARG_ACCOUNT_ID]
    private var existing: Account? = null
    private var balance: Money? = null

    private val _state = MutableStateFlow(AccountEditUiState(isLoading = true, isNew = accountId == null))
    val state: StateFlow<AccountEditUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val all = accounts.observeAccounts(includeArchived = true).first()
        // Currencies already in use come first, then the family's usual ones.
        val byUsage = all.groupingBy { it.currency.value }.eachCount().entries
            .sortedByDescending { it.value }.map { it.key }
        val suggested = (byUsage + DEFAULT_CURRENCIES).distinct()

        val account = accountId?.let { accounts.getAccount(it) }
        existing = account
        balance = account?.let { a -> accounts.observeAccountsWithBalance(includeArchived = true).first().firstOrNull { it.account.id == a.id }?.balance }
        val showSharing = otherMembers.names().first().isNotEmpty()
        _state.update {
            if (account == null) {
                it.copy(isLoading = false, isNew = true, currency = suggested.first(), suggestedCurrencies = suggested, showSharing = showSharing)
            } else {
                it.copy(
                    isLoading = false,
                    isNew = false,
                    name = account.name,
                    currency = account.currency.value,
                    type = account.type,
                    initialBalance = formatForEditing(account),
                    archived = account.archived,
                    includedInTotal = !account.excludedFromTotal,
                    color = account.color,
                    cardDigits = account.cardDigits.orEmpty(),
                    shared = account.isShared,
                    showSharing = showSharing,
                    currencyLocked = saveAccount.isCurrencyLocked(account.id),
                    suggestedCurrencies = suggested,
                    currentBalance = balance?.let(moneyFormat::format).orEmpty(),
                )
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, nameError = false) }

    fun onCurrencyChange(value: String) = _state.update {
        if (it.currencyLocked) it else it.copy(currency = value.trim().uppercase(), currencyError = null)
    }

    fun onTypeChange(value: AccountType) = _state.update { it.copy(type = value) }

    fun onInitialBalanceChange(value: String) = _state.update { it.copy(initialBalance = value, balanceError = null) }

    /** The real balance typed in «Исправить баланс»: the signed difference is shown right away. */
    fun onActualBalanceChange(value: String) = _state.update {
        val actual = parseActual(value)
        val diff = (actual as? ParsedActual.Amount)?.money?.let { m -> balance?.let { m - it } }
        it.copy(actualBalance = value, actualBalanceError = (actual as? ParsedActual.Bad)?.error, adjustment = diff?.let(::signed))
    }

    /**
     * «Сохранить баланс» (#581, poll A): records the adjustment right away, without «Сохранить» of the account;
     * the form stays open with the new balance and says what was recorded.
     */
    fun saveBalance() {
        val account = existing ?: return
        val actual = parseActual(_state.value.actualBalance) as? ParsedActual.Amount ?: return
        val diff = balance?.let { actual.money - it }
        viewModelScope.launch {
            if (adjustBalance(account.id, actual.money) !is AdjustBalanceUseCase.Result.Adjusted) return@launch
            balance = actual.money
            _state.update {
                it.copy(
                    currentBalance = moneyFormat.format(actual.money),
                    actualBalance = "",
                    adjustment = null,
                    balanceSaved = diff?.let(::signed),
                )
            }
        }
    }

    fun dismissBalanceSaved() = _state.update { it.copy(balanceSaved = null) }

    fun onIncludedInTotalChange(value: Boolean) = _state.update { it.copy(includedInTotal = value) }

    fun onColorChange(value: Long?) = _state.update { it.copy(color = value) }

    /** «Для чеков» (#577): only digits, at most [Account.CARD_DIGITS] of them. */
    fun onCardDigitsChange(value: String) =
        _state.update { it.copy(cardDigits = value.filter { c -> c in '0'..'9' }.take(Account.CARD_DIGITS), cardDigitsError = false) }

    fun onSharedChange(value: Boolean) = _state.update { it.copy(shared = value) }

    fun save() {
        val current = _state.value
        if (current.isLoading || current.isDone) return
        val account = buildAccount(current) ?: return
        val actual = parseActual(current.actualBalance)
        if (actual is ParsedActual.Bad) {
            _state.update { it.copy(actualBalanceError = actual.error) }
            return
        }
        viewModelScope.launch {
            when (val result = saveAccount(account)) {
                SaveAccountUseCase.Result.Saved -> {
                    // After the account: a changed initial balance is already counted, the actual one is what stays.
                    if (actual is ParsedActual.Amount && existing != null) adjustBalance(account.id, actual.money)
                    // A new account outside the totals currencies: the «currency_account» warm pill (#513).
                    val foreign = existing == null && account.currency !in reportCurrencies.currencies.first()
                    _state.update { it.copy(isDone = true, foreignCurrencyAdded = foreign) }
                }
                is SaveAccountUseCase.Result.Invalid -> when (result.reason) {
                    SaveAccountUseCase.Reason.CURRENCY_LOCKED ->
                        _state.update { it.copy(currencyError = CurrencyError.LOCKED, currencyLocked = true) }
                }
            }
        }
    }

    /** Archives or restores an existing account and closes the screen. Unsaved edits are dropped. */
    fun toggleArchived() {
        val account = existing ?: return
        viewModelScope.launch {
            accounts.saveAccount(account.copy(archived = !account.archived))
            _state.update { it.copy(isDone = true) }
        }
    }

    fun delete() {
        val account = existing ?: return
        viewModelScope.launch {
            when (deleteAccount(account.id)) {
                DeleteAccountUseCase.Result.DELETED -> _state.update { it.copy(isDone = true) }
                DeleteAccountUseCase.Result.HAS_TRANSACTIONS -> _state.update { it.copy(deleteBlocked = true) }
            }
        }
    }

    fun dismissDeleteBlocked() = _state.update { it.copy(deleteBlocked = false) }

    /** Validates the form; on errors updates the state and returns null. */
    private fun buildAccount(form: AccountEditUiState): Account? {
        val name = form.name.trim()
        val currency = CurrencyCode.parseOrNull(form.currency)
        var balanceError: BalanceError? = null
        val balanceMinor = when {
            currency == null -> null
            form.initialBalance.isBlank() -> 0L
            else -> when (val parsed = MoneyParser.parse(form.initialBalance, currency)) {
                is MoneyParser.Result.Success -> parsed.money.amountMinor
                is MoneyParser.Result.Error -> {
                    balanceError = if (parsed.reason == MoneyParser.Reason.TOO_MANY_FRACTION_DIGITS) {
                        BalanceError.TOO_MANY_FRACTION_DIGITS
                    } else {
                        BalanceError.INVALID
                    }
                    null
                }
            }
        }
        if (name.isEmpty() || currency == null || balanceMinor == null) {
            _state.update {
                it.copy(
                    nameError = name.isEmpty(),
                    currencyError = if (currency == null) CurrencyError.INVALID else null,
                    balanceError = balanceError,
                )
            }
            return null
        }
        val digits = form.cardDigits.trim()
        if (digits.isNotEmpty() && digits.length != Account.CARD_DIGITS) {
            _state.update { it.copy(cardDigitsError = true) }
            return null
        }
        val base = existing ?: Account(id = ids.newId(), name = name, currency = currency)
        return base.copy(
            cardDigits = digits.ifEmpty { null },
            name = name,
            currency = currency,
            type = form.type,
            initialBalanceMinor = balanceMinor,
            excludedFromTotal = !form.includedInTotal,
            color = form.color,
            isShared = form.shared,
            // The repository makes the current member the owner of a new personal account.
            ownerMemberId = if (form.shared) null else base.ownerMemberId,
        )
    }

    private sealed interface ParsedActual {
        data object None : ParsedActual
        data class Amount(val money: Money) : ParsedActual
        data class Bad(val error: BalanceError) : ParsedActual
    }

    /** Blank — no adjustment; parsed in the account's saved currency (an account with a balance has its currency locked). */
    private fun parseActual(text: String): ParsedActual {
        val currency = balance?.currency ?: return ParsedActual.None
        if (text.isBlank()) return ParsedActual.None
        return when (val parsed = MoneyParser.parse(text, currency)) {
            is MoneyParser.Result.Success -> ParsedActual.Amount(parsed.money)
            is MoneyParser.Result.Error -> ParsedActual.Bad(
                if (parsed.reason == MoneyParser.Reason.TOO_MANY_FRACTION_DIGITS) BalanceError.TOO_MANY_FRACTION_DIGITS else BalanceError.INVALID,
            )
        }
    }

    private fun signed(diff: Money): String? = when {
        diff.amountMinor > 0 -> "+" + moneyFormat.format(diff)
        diff.amountMinor < 0 -> "\u2212" + moneyFormat.format(-diff)
        else -> null
    }

    private fun formatForEditing(account: Account): String =
        if (account.initialBalanceMinor == 0L) "" else moneyFormat.formatForEditing(account.initialBalance)

    companion object {
        /** The family lives in Argentina, earns in AMD and keeps RUB accounts (see docs/agents/owner.md). */
        val DEFAULT_CURRENCIES = listOf("ARS", "USD", "AMD", "RUB", "EUR", "USDT")
    }
}

/** Tests and previews: the default totals currencies, read-only. */
private object DefaultReportCurrencies : ReportCurrencySettings {
    override val currencies: Flow<List<CurrencyCode>> = flowOf(ReportCurrencySettings.DEFAULT)
    override suspend fun setCurrencies(currencies: List<CurrencyCode>) = Unit
}

package app.coshell.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.accounts.AccountEditUiState.BalanceError
import app.coshell.accounts.AccountEditUiState.CurrencyError
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.DeleteAccountUseCase
import app.coshell.core.domain.SaveAccountUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.MoneyParser
import app.coshell.navigation.AccountEditDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
) : ViewModel() {

    private val accountId: String? = savedStateHandle[AccountEditDestination.ARG_ACCOUNT_ID]
    private var existing: Account? = null

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
        _state.update {
            if (account == null) {
                it.copy(isLoading = false, isNew = true, currency = suggested.first(), suggestedCurrencies = suggested)
            } else {
                it.copy(
                    isLoading = false,
                    isNew = false,
                    name = account.name,
                    currency = account.currency.value,
                    type = account.type,
                    initialBalance = formatForEditing(account),
                    archived = account.archived,
                    currencyLocked = saveAccount.isCurrencyLocked(account.id),
                    suggestedCurrencies = suggested,
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

    fun save() {
        val current = _state.value
        if (current.isLoading || current.isDone) return
        val account = buildAccount(current) ?: return
        viewModelScope.launch {
            when (val result = saveAccount(account)) {
                SaveAccountUseCase.Result.Saved -> _state.update { it.copy(isDone = true) }
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
        val base = existing ?: Account(id = ids.newId(), name = name, currency = currency)
        return base.copy(name = name, currency = currency, type = form.type, initialBalanceMinor = balanceMinor)
    }

    private fun formatForEditing(account: Account): String =
        if (account.initialBalanceMinor == 0L) "" else moneyFormat.formatForEditing(account.initialBalance)

    companion object {
        /** The family lives in Argentina, earns in AMD and keeps RUB accounts (see docs/agents/owner.md). */
        val DEFAULT_CURRENCIES = listOf("ARS", "USD", "AMD", "RUB", "EUR", "USDT")
    }
}

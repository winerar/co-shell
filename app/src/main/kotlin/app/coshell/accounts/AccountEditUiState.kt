package app.coshell.accounts

import app.coshell.core.model.AccountType

data class AccountEditUiState(
    val isLoading: Boolean = false,
    val isNew: Boolean = true,
    val name: String = "",
    /** Currency code as typed, e.g. "ARS". */
    val currency: String = "",
    val type: AccountType = AccountType.CARD,
    /** Initial balance as typed; empty means zero. */
    val initialBalance: String = "",
    val archived: Boolean = false,
    /** The account has transactions, so its currency cannot be changed. */
    val currencyLocked: Boolean = false,
    val suggestedCurrencies: List<String> = emptyList(),
    val nameError: Boolean = false,
    val currencyError: CurrencyError? = null,
    val balanceError: BalanceError? = null,
    /** Delete was refused because the account has transactions; offer archiving instead. */
    val deleteBlocked: Boolean = false,
    /** Saved or deleted: the screen should close. */
    val isDone: Boolean = false,
) {
    enum class CurrencyError { INVALID, LOCKED }
    enum class BalanceError { INVALID, TOO_MANY_FRACTION_DIGITS }
}

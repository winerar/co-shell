package app.coshell.accounts.edit

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
    /** Counted in the total balance on «Обзор»; off for debts and someone else's money. */
    val includedInTotal: Boolean = true,
    /** Colour of the account's card (category palette), `null` — no colour. */
    val color: Long? = null,
    /** Shared by the family; off — personal account of its owner (still visible to everyone). */
    val shared: Boolean = true,
    /** «Только у меня» (#558): the account and its operations stay on this phone. */
    val localOnly: Boolean = false,
    /** The family has other members, so «shared / personal» means something. */
    val showSharing: Boolean = false,
    /**
     * «Только у меня» can be switched on only while creating (a saved account may already be in the cloud);
     * switching it off later is always possible (the account and its operations then go to the family).
     */
    val localOnlyLocked: Boolean = false,
    /** The account has transactions, so its currency cannot be changed. */
    val currencyLocked: Boolean = false,
    val suggestedCurrencies: List<String> = emptyList(),
    val nameError: Boolean = false,
    val currencyError: CurrencyError? = null,
    val balanceError: BalanceError? = null,
    /** Balance now, formatted with the code («245 300,00 ARS»); empty for a new account. */
    val currentBalance: String = "",
    /** The real balance as typed in «Исправить баланс»; empty — no adjustment. */
    val actualBalance: String = "",
    val actualBalanceError: BalanceError? = null,
    /** The difference that will be recorded on save, signed («−2 500,00 ARS»); `null` — none. */
    val adjustment: String? = null,
    /** «Сохранить баланс» recorded this adjustment («+12 000,00 ARS»): the «Баланс исправлен» pill; `null` — nothing to say. */
    val balanceSaved: String? = null,
    /** Delete was refused because the account has transactions; offer archiving instead. */
    val deleteBlocked: Boolean = false,
    /** Saved or deleted: the screen should close. */
    val isDone: Boolean = false,
    /** Closing after creating an account outside the totals currencies: the «currency_account» pill (#513). */
    val foreignCurrencyAdded: Boolean = false,
) {
    /** Only a saved account has a balance to correct. */
    val canAdjust: Boolean get() = !isNew

    enum class CurrencyError { INVALID, LOCKED }
    enum class BalanceError { INVALID, TOO_MANY_FRACTION_DIGITS }
}

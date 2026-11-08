package app.coshell.core.model

/** Where money lives: a wallet, a card, a deposit. Every account has exactly one currency. */
data class Account(
    val id: AccountId,
    val name: String,
    val currency: CurrencyCode,
    val type: AccountType,
    /** Initial balance in minor units of [currency]; may be negative (credit card). */
    val initialBalanceMinor: Long = 0,
    val isShared: Boolean = true,
    val ownerMemberId: MemberId? = null,
    val archived: Boolean = false,
    val meta: SyncMeta,
) {
    init {
        require(name.isNotBlank()) { "Account name must not be blank" }
    }

    val initialBalance: Money get() = Money(initialBalanceMinor, currency)
}

enum class AccountType { CASH, CARD, DEPOSIT, SAVINGS, CREDIT, OTHER }

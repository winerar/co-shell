package app.coshell.core.model

/**
 * A place where money lives: a card, a cash wallet, a deposit, a crypto exchange balance.
 * Every account has exactly one [currency]; all its transactions are stored in that currency.
 */
data class Account(
    val id: String,
    val name: String,
    val currency: CurrencyCode,
    val type: AccountType = AccountType.CARD,
    /** Balance before the first recorded transaction, in minor units of [currency]. */
    val initialBalanceMinor: Long = 0L,
    val isShared: Boolean = true,
    val ownerMemberId: String? = null,
    val archived: Boolean = false,
    val sortOrder: Int = 0,
    /** Left out of the total balance on «Обзор» (a debt, someone else's money). */
    val excludedFromTotal: Boolean = false,
) {
    init {
        require(name.isNotBlank()) { "Account name must not be blank" }
    }

    val initialBalance: Money get() = Money(initialBalanceMinor, currency)
}

enum class AccountType { CASH, CARD, DEPOSIT, CRYPTO, OTHER }

/** An account together with its current balance (initial balance + all its transactions). */
data class AccountWithBalance(val account: Account, val balance: Money)

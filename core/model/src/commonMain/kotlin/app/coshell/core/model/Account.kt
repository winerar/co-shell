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
    /** Colour of the account's card and badge as 0xAARRGGBB (the category palette), `null` — no colour. */
    val color: Long? = null,
    /** Last [CARD_DIGITS] digits of the card or the account number as a bank receipt prints them (#577), `null` — not linked. */
    val cardDigits: String? = null,
    /**
     * A prepaid card — a travel pass (SUBE, «Тройка»; #645): a top-up is an ordinary expense from a card, so the balance on the card is
     * a number the person checks by hand («Сверить баланс»). Stored as type [AccountType.OTHER] + this flag: an older version of the
     * app, which cannot read a new type, still shows the account.
     */
    val prepaid: Boolean = false,
    /**
     * A credit card (#686): its conditions; `null` — an ordinary account. Stored as type [AccountType.OTHER] + these fields for the same
     * reason as [prepaid]: an older version cannot read a new type. The balance is the debt when it is negative.
     */
    val credit: CreditTerms? = null,
) {
    init {
        require(name.isNotBlank()) { "Account name must not be blank" }
        require(cardDigits == null || (cardDigits.length == CARD_DIGITS && cardDigits.all { it in '0'..'9' })) { "Card digits must be $CARD_DIGITS digits" }
    }

    val initialBalance: Money get() = Money(initialBalanceMinor, currency)

    companion object {
        const val CARD_DIGITS = 4

        /** «4083********3900», «1570 0000 0000 0001», «3900» → the last [CARD_DIGITS] digits; `null` when there are fewer. */
        fun digitsOf(text: String): String? = text.filter { it in '0'..'9' }.takeIf { it.length >= CARD_DIGITS }?.takeLast(CARD_DIGITS)
    }
}

enum class AccountType { CASH, CARD, DEPOSIT, CRYPTO, OTHER }

/** An account together with its current balance (initial balance + all its transactions). */
data class AccountWithBalance(val account: Account, val balance: Money)

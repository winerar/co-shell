package app.coshell.core.model

/**
 * A saved "blank" for an operation that repeats (rent, lessons, coffee): everything but the date.
 *
 * [currency] is the currency of [accountId]; it is stored so that a template without an amount
 * still shows its currency, and so that a changed account can be noticed. [amount] is optional:
 * `null` means "ask every time". It is usually in [currency]; an expense or income may name another
 * one (rent 650 USD paid from an ARS account, #415) — then it is the receipt, and what leaves the
 * account is counted by the rate of the day ([isForeign]). A transfer template remembers the target
 * account, the amount that arrives is entered when the template is used.
 */
data class TransactionTemplate(
    val id: String,
    val name: String,
    val type: TransactionType,
    val accountId: String,
    val currency: CurrencyCode,
    val amount: Money? = null,
    val categoryId: String? = null,
    val toAccountId: String? = null,
    val note: String = "",
    val sortOrder: Int = 0,
    /** «Повтор» — a regular payment (#143); `null` — an ordinary template. */
    val schedule: RecurringSchedule? = null,
) {
    init {
        require(name.isNotBlank()) { "Template name must not be blank" }
        if (schedule?.mode == PostMode.AUTO) {
            require(amount != null) { "Only a template with an amount posts by itself" }
            require(type != TransactionType.TRANSFER) { "A transfer is posted by hand: the arriving amount is entered then" }
        }
        amount?.let {
            require(it.isPositive) { "Template amount must be positive, got $it" }
            require(it.currency == currency || type != TransactionType.TRANSFER) {
                "A transfer template amount must be in the account currency"
            }
        }
        when (type) {
            TransactionType.TRANSFER -> {
                requireNotNull(toAccountId) { "Transfer template needs toAccountId" }
                require(toAccountId != accountId) { "Transfer to the same account" }
            }
            TransactionType.EXPENSE, TransactionType.INCOME ->
                require(toAccountId == null) { "Only transfers have a target account" }
        }
    }

    /** The amount is in another currency than the account's (#415): posted by the rate of the day, kept as the receipt. */
    val isForeign: Boolean get() = amount != null && amount.currency != currency
}

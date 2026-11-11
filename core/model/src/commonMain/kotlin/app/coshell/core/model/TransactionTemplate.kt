package app.coshell.core.model

/**
 * A saved "blank" for an operation that repeats (rent, lessons, coffee): everything but the date.
 *
 * [currency] is the currency of [accountId]; it is stored so that a template without an amount
 * still shows its currency, and so that a changed account can be noticed. [amount] is optional:
 * `null` means "ask every time". A transfer template remembers the target account, the amount
 * that arrives is entered when the template is used.
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
) {
    init {
        require(name.isNotBlank()) { "Template name must not be blank" }
        amount?.let {
            require(it.isPositive) { "Template amount must be positive, got $it" }
            require(it.currency == currency) { "Template amount must be in the account currency" }
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
}

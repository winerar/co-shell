package app.coshell.core.model

import kotlin.time.Instant

/**
 * A single money movement.
 *
 * Amounts are always **positive**; the direction comes from [type]:
 * - [TransactionType.EXPENSE] — [amount] leaves [accountId];
 * - [TransactionType.INCOME] — [amount] arrives to [accountId];
 * - [TransactionType.TRANSFER] — [amount] leaves [accountId] and [toAmount] arrives to
 *   [toAccountId]. The two amounts may be in different currencies: a currency exchange is just
 *   a transfer between accounts in different currencies, the real rate is `toAmount / amount`.
 *
 * [amount] is in the currency of [accountId], [toAmount] in the currency of [toAccountId]
 * (checked by [app.coshell.core.domain] use cases, which know the accounts).
 */
data class Transaction(
    val id: String,
    val type: TransactionType,
    val accountId: String,
    val amount: Money,
    val date: Instant,
    val categoryId: String? = null,
    val toAccountId: String? = null,
    val toAmount: Money? = null,
    val note: String = "",
    val payee: String? = null,
    val labels: List<String> = emptyList(),
    /** What the receipt said when paid in a currency other than the account's. */
    val originalAmount: Money? = null,
    val authorMemberId: String? = null,
    /** Where the record was imported from (`"wallet"`), `null` for records entered in the app. */
    val importSource: String? = null,
    /** Stable id of the record in [importSource]; repeated imports skip known ids. */
    val importExternalId: String? = null,
    /** One import run (one file loaded at once): «отменить импорт» removes the whole batch. */
    val importBatchId: String? = null,
) {
    init {
        require(amount.isPositive) { "Transaction amount must be positive, got $amount" }
        originalAmount?.let { require(it.isPositive) { "Original amount must be positive" } }
        when (type) {
            TransactionType.TRANSFER -> {
                requireNotNull(toAccountId) { "Transfer needs toAccountId" }
                requireNotNull(toAmount) { "Transfer needs toAmount" }
                require(toAccountId != accountId) { "Transfer to the same account" }
                require(toAmount.isPositive) { "Transfer toAmount must be positive" }
            }
            TransactionType.EXPENSE, TransactionType.INCOME -> {
                require(toAccountId == null && toAmount == null) { "Only transfers have a target account" }
            }
        }
        require((importSource == null) == (importExternalId == null)) {
            "importSource and importExternalId go together"
        }
        require(importBatchId == null || importSource != null) { "Only imported records belong to a batch" }
    }

    /** How this transaction changes the balance of [account], in that account's currency. */
    fun effectOn(account: String): Money? = when {
        type == TransactionType.EXPENSE && accountId == account -> -amount
        type == TransactionType.INCOME && accountId == account -> amount
        type == TransactionType.TRANSFER && accountId == account -> -amount
        type == TransactionType.TRANSFER && toAccountId == account -> toAmount
        else -> null
    }
}

enum class TransactionType { EXPENSE, INCOME, TRANSFER }

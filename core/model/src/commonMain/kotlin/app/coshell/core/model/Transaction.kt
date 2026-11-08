package app.coshell.core.model

import kotlinx.datetime.LocalDate

/**
 * A money movement. Amounts are always positive; the direction comes from [type].
 *
 * - EXPENSE / INCOME: [amount] in the currency of [accountId], with an optional category.
 * - TRANSFER: [amount] leaves [accountId], [toAmount] arrives to [toAccountId] in that account's
 *   currency. A currency exchange is a transfer between accounts in different currencies, so the
 *   real rate is recorded implicitly.
 *
 * [originalAmount] is what the receipt said when paid in a foreign currency (a RUB card in EUR).
 * Currency consistency with the accounts is checked by the domain layer, which knows the accounts.
 */
data class Transaction(
    val id: TransactionId,
    val type: TransactionType,
    val accountId: AccountId,
    val amount: Money,
    val date: LocalDate,
    val categoryId: CategoryId? = null,
    val toAccountId: AccountId? = null,
    val toAmount: Money? = null,
    val originalAmount: Money? = null,
    val note: String = "",
    val payee: String? = null,
    val labels: List<String> = emptyList(),
    val authorMemberId: MemberId? = null,
    val importSource: String? = null,
    val importExternalId: String? = null,
    val meta: SyncMeta,
) {
    init {
        require(amount.isPositive) { "Amount must be positive, direction comes from type" }
        if (type == TransactionType.TRANSFER) {
            requireNotNull(toAccountId) { "Transfer needs toAccountId" }
            requireNotNull(toAmount) { "Transfer needs toAmount" }
            require(toAccountId != accountId) { "Transfer to the same account" }
            require(toAmount.isPositive) { "toAmount must be positive" }
            require(categoryId == null) { "Transfer has no category" }
            require(originalAmount == null) { "Transfer has no original amount" }
        } else {
            require(toAccountId == null && toAmount == null) { "Only transfers have a target account" }
        }
        originalAmount?.let {
            require(it.isPositive) { "originalAmount must be positive" }
            require(it.currency != amount.currency) { "originalAmount is only for a foreign currency" }
        }
        require(labels.none { it.isBlank() }) { "Labels must not be blank" }
        require((importSource == null) == (importExternalId == null)) {
            "importSource and importExternalId go together"
        }
    }

    /** Signed change of [account]'s balance caused by this transaction (zero if unrelated). */
    fun effectOn(account: AccountId): Long = when {
        meta.deleted -> 0
        type == TransactionType.EXPENSE && account == accountId -> -amount.amountMinor
        type == TransactionType.INCOME && account == accountId -> amount.amountMinor
        type == TransactionType.TRANSFER && account == accountId -> -amount.amountMinor
        type == TransactionType.TRANSFER && account == toAccountId -> toAmount!!.amountMinor
        else -> 0
    }
}

enum class TransactionType { EXPENSE, INCOME, TRANSFER }

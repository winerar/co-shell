package app.coshell.core.domain

import app.coshell.core.model.CategoryType
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType

/**
 * Validates a transaction against the accounts and category it references, then saves it.
 * Structural rules (positive amounts, transfer has a target…) are enforced by [Transaction] itself.
 */
class SaveTransactionUseCase(
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
) {
    sealed interface Result {
        data object Saved : Result
        data class Invalid(val reason: Reason) : Result
    }

    enum class Reason {
        ACCOUNT_NOT_FOUND,
        TARGET_ACCOUNT_NOT_FOUND,
        CURRENCY_MISMATCH,
        TARGET_CURRENCY_MISMATCH,
        CATEGORY_NOT_FOUND,
        CATEGORY_TYPE_MISMATCH,
        /** Receipt amount in the account's own currency, or on a transfer. */
        ORIGINAL_CURRENCY_INVALID,
    }

    suspend operator fun invoke(transaction: Transaction): Result {
        val account = accounts.getAccount(transaction.accountId)
            ?: return invalid(Reason.ACCOUNT_NOT_FOUND)
        if (account.currency != transaction.amount.currency) return invalid(Reason.CURRENCY_MISMATCH)
        transaction.originalAmount?.let { original ->
            if (transaction.type == TransactionType.TRANSFER || original.currency == account.currency) {
                return invalid(Reason.ORIGINAL_CURRENCY_INVALID)
            }
        }

        if (transaction.type == TransactionType.TRANSFER) {
            val target = transaction.toAccountId?.let { accounts.getAccount(it) }
                ?: return invalid(Reason.TARGET_ACCOUNT_NOT_FOUND)
            if (target.currency != transaction.toAmount?.currency) return invalid(Reason.TARGET_CURRENCY_MISMATCH)
        }

        transaction.categoryId?.let { categoryId ->
            val category = categories.getCategory(categoryId) ?: return invalid(Reason.CATEGORY_NOT_FOUND)
            val expected = when (transaction.type) {
                TransactionType.EXPENSE -> CategoryType.EXPENSE
                TransactionType.INCOME -> CategoryType.INCOME
                TransactionType.TRANSFER -> null // transfers may carry any category (e.g. fees)
            }
            if (expected != null && category.type != expected) return invalid(Reason.CATEGORY_TYPE_MISMATCH)
        }

        transactions.saveTransaction(transaction)
        return Result.Saved
    }

    private fun invalid(reason: Reason) = Result.Invalid(reason)
}

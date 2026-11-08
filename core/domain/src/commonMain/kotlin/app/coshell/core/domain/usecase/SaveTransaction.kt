package app.coshell.core.domain.usecase

import app.coshell.core.domain.repository.AccountRepository
import app.coshell.core.domain.repository.CategoryRepository
import app.coshell.core.domain.repository.TransactionRepository
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType

/**
 * Validates a transaction against the accounts and categories it references, then saves it.
 * Rules that need only the transaction itself live in [Transaction]'s constructor.
 */
class SaveTransaction(
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
) {
    suspend operator fun invoke(transaction: Transaction): Result {
        val error = validate(transaction)
        if (error != null) return Result.Invalid(error)
        transactions.upsert(transaction)
        return Result.Saved
    }

    private suspend fun validate(tx: Transaction): Error? {
        val account = accounts.getAccount(tx.accountId) ?: return Error.ACCOUNT_NOT_FOUND
        if (tx.amount.currency != account.currency) return Error.CURRENCY_MISMATCH

        if (tx.type == TransactionType.TRANSFER) {
            val target = accounts.getAccount(tx.toAccountId!!) ?: return Error.ACCOUNT_NOT_FOUND
            if (tx.toAmount!!.currency != target.currency) return Error.CURRENCY_MISMATCH
        }

        val categoryId = tx.categoryId ?: return null
        val category = categories.getCategory(categoryId) ?: return Error.CATEGORY_NOT_FOUND
        val expected = if (tx.type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
        return if (category.type != expected) Error.CATEGORY_TYPE_MISMATCH else null
    }

    sealed interface Result {
        data object Saved : Result
        data class Invalid(val error: Error) : Result
    }

    enum class Error {
        ACCOUNT_NOT_FOUND,
        /** Amount is not in the account's currency (or toAmount not in the target's). */
        CURRENCY_MISMATCH,
        CATEGORY_NOT_FOUND,
        /** E.g. an expense filed under an income category. */
        CATEGORY_TYPE_MISMATCH,
    }
}

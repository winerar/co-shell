package app.coshell.core.domain

import app.coshell.core.model.CategoryType
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

/**
 * Brings an account's balance to what the person actually has: records the difference as one
 * operation in «Корректировка баланса» (income if there is more money, expense if less), dated now.
 * Such operations change the balance but stay out of spending, income and budgets ([SystemCategories]).
 */
class AdjustBalanceUseCase(
    private val accounts: AccountRepository,
    private val transactions: TransactionRepository,
    private val ensureAdjustment: EnsureAdjustmentCategoriesUseCase,
    private val ids: IdGenerator,
    private val clock: Clock = Clock.System,
) {
    sealed interface Result {
        /** [transaction] was saved; the balance is now the actual one. */
        data class Adjusted(val transaction: Transaction) : Result
        /** The balance already equals the actual one: nothing recorded. */
        data object Unchanged : Result
        data class Invalid(val reason: Reason) : Result
    }

    enum class Reason { ACCOUNT_NOT_FOUND, CURRENCY_MISMATCH }

    /** The difference [actual] − current balance of [accountId]; `null` if the account is unknown or the currency differs. */
    suspend fun difference(accountId: String, actual: Money): Money? {
        val current = currentBalance(accountId) ?: return null
        if (current.currency != actual.currency) return null
        return actual - current
    }

    suspend operator fun invoke(accountId: String, actual: Money): Result {
        val current = currentBalance(accountId) ?: return Result.Invalid(Reason.ACCOUNT_NOT_FOUND)
        if (current.currency != actual.currency) return Result.Invalid(Reason.CURRENCY_MISMATCH)
        val diff = actual - current
        if (diff.amountMinor == 0L) return Result.Unchanged
        val categories = ensureAdjustment()
        val type = if (diff.isPositive) TransactionType.INCOME else TransactionType.EXPENSE
        val transaction = Transaction(
            id = ids.newId(),
            type = type,
            accountId = accountId,
            amount = if (diff.isPositive) diff else -diff,
            date = clock.now(),
            categoryId = categories.getValue(if (diff.isPositive) CategoryType.INCOME else CategoryType.EXPENSE),
        )
        transactions.saveTransaction(transaction)
        return Result.Adjusted(transaction)
    }

    private suspend fun currentBalance(accountId: String): Money? =
        accounts.observeAccountsWithBalance(includeArchived = true).first()
            .firstOrNull { it.account.id == accountId }?.balance
}

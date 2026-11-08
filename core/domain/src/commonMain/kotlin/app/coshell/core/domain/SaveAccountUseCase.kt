package app.coshell.core.domain

import app.coshell.core.model.Account
import kotlinx.coroutines.flow.first

/**
 * Creates or updates an account.
 *
 * - The currency of an account that already has transactions cannot change: its stored amounts
 *   would silently switch currency.
 * - A new account is placed after all existing ones (its [Account.sortOrder] is assigned here).
 */
class SaveAccountUseCase(
    private val accounts: AccountRepository,
    private val transactions: TransactionRepository,
) {
    sealed interface Result {
        data object Saved : Result
        data class Invalid(val reason: Reason) : Result
    }

    enum class Reason { CURRENCY_LOCKED }

    suspend operator fun invoke(account: Account): Result {
        val existing = accounts.getAccount(account.id)
        if (existing == null) {
            val nextOrder = accounts.observeAccounts(includeArchived = true).first()
                .maxOfOrNull { it.sortOrder + 1 } ?: 0
            accounts.saveAccount(account.copy(sortOrder = nextOrder))
            return Result.Saved
        }
        if (existing.currency != account.currency && isCurrencyLocked(account.id)) {
            return Result.Invalid(Reason.CURRENCY_LOCKED)
        }
        accounts.saveAccount(account)
        return Result.Saved
    }

    /** True when the account's currency can no longer be changed. */
    suspend fun isCurrencyLocked(accountId: String): Boolean = transactions.countForAccount(accountId) > 0
}

package app.coshell.core.domain

/**
 * Deletes an account only if nothing references it; otherwise the user should archive it
 * (archived accounts keep their history and balance but are hidden from pickers).
 */
class DeleteAccountUseCase(
    private val accounts: AccountRepository,
    private val transactions: TransactionRepository,
) {
    enum class Result { DELETED, HAS_TRANSACTIONS }

    suspend operator fun invoke(accountId: String): Result {
        if (transactions.countForAccount(accountId) > 0) return Result.HAS_TRANSACTIONS
        accounts.deleteAccount(accountId)
        return Result.DELETED
    }
}

/** Same rule for categories: used categories are archived, not deleted. */
class DeleteCategoryUseCase(
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
) {
    enum class Result { DELETED, HAS_TRANSACTIONS }

    suspend operator fun invoke(categoryId: String): Result {
        if (transactions.countForCategory(categoryId) > 0) return Result.HAS_TRANSACTIONS
        categories.deleteCategory(categoryId)
        return Result.DELETED
    }
}

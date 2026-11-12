package app.coshell.core.domain

import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

// Repository contracts. Implementations live in core:data (Room). Deletes are soft
// (tombstones for sync): deleted records disappear from every read below.

interface AccountRepository {
    fun observeAccounts(includeArchived: Boolean = false): Flow<List<Account>>

    /** Accounts with balances = initial balance + effect of every transaction. */
    fun observeAccountsWithBalance(includeArchived: Boolean = false): Flow<List<AccountWithBalance>>

    suspend fun getAccount(id: String): Account?
    suspend fun saveAccount(account: Account)
    suspend fun deleteAccount(id: String)
}

interface CategoryRepository {
    fun observeCategories(type: CategoryType? = null, includeArchived: Boolean = false): Flow<List<Category>>
    suspend fun getCategory(id: String): Category?
    suspend fun saveCategory(category: Category)
    suspend fun deleteCategory(id: String)

    /** Number of categories including archived ones (but not deleted). */
    suspend fun countCategories(): Int
    suspend fun saveCategories(categories: List<Category>)
}

data class TransactionFilter(
    /** Matches both the source and the target account of transfers. */
    val accountId: String? = null,
    val categoryId: String? = null,
    /** Inclusive. */
    val from: Instant? = null,
    /** Exclusive. */
    val to: Instant? = null,
    /** At most this many newest transactions; null — all. The list grows this window while scrolling. */
    val limit: Int? = null,
)

interface TransactionRepository {
    /** Newest first. */
    fun observeTransactions(filter: TransactionFilter = TransactionFilter()): Flow<List<Transaction>>
    suspend fun getTransaction(id: String): Transaction?
    suspend fun saveTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)

    /** Number of live transactions that reference the account (as source or target). */
    suspend fun countForAccount(accountId: String): Int
    suspend fun countForCategory(categoryId: String): Int
}

/** Full local backup as JSON (manual now, weekly automatic in stage 4). */
interface BackupRepository {
    suspend fun exportJson(): String

    /**
     * Adds or overwrites (by id) every record from [json]; records absent from the backup stay.
     * Atomic: on any error nothing is written.
     */
    suspend fun importJson(json: String): BackupImportResult
}

sealed interface BackupImportResult {
    data class Imported(
        val accounts: Int,
        val categories: Int,
        val transactions: Int,
        val budgets: Int,
        val manualRates: Int = 0,
        val templates: Int = 0,
    ) : BackupImportResult
    data object NotABackup : BackupImportResult
    data class UnsupportedVersion(val version: Int) : BackupImportResult
    data class Corrupted(val message: String) : BackupImportResult
}

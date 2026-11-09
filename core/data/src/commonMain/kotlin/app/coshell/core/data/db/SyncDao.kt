package app.coshell.core.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import app.coshell.core.data.sync.Version
import app.coshell.core.data.sync.remoteWins
import kotlinx.coroutines.flow.Flow

/** Rows downloaded in one pull, grouped by table. */
data class PulledRows(
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val manualRates: List<ManualRateEntity> = emptyList(),
)

/** Rows waiting to be sent to the server. */
data class PendingRows(
    val accounts: List<AccountEntity>,
    val categories: List<CategoryEntity>,
    val transactions: List<TransactionEntity>,
    val budgets: List<BudgetEntity>,
    val manualRates: List<ManualRateEntity>,
) {
    val size get() = accounts.size + categories.size + transactions.size + budgets.size + manualRates.size
}

/** Queries for SyncEngine. Unlike the screen DAOs they see soft-deleted rows too. */
@Dao
abstract class SyncDao {
    // --- pending rows ---
    @Query("SELECT * FROM accounts WHERE pending_sync = 1") abstract suspend fun pendingAccounts(): List<AccountEntity>
    @Query("SELECT * FROM categories WHERE pending_sync = 1") abstract suspend fun pendingCategories(): List<CategoryEntity>
    @Query("SELECT * FROM transactions WHERE pending_sync = 1") abstract suspend fun pendingTransactions(): List<TransactionEntity>
    @Query("SELECT * FROM budgets WHERE pending_sync = 1") abstract suspend fun pendingBudgets(): List<BudgetEntity>
    @Query("SELECT * FROM manual_rates WHERE pending_sync = 1") abstract suspend fun pendingManualRates(): List<ManualRateEntity>

    @Query(
        """SELECT (SELECT COUNT(*) FROM accounts WHERE pending_sync = 1) + (SELECT COUNT(*) FROM categories WHERE pending_sync = 1)
           + (SELECT COUNT(*) FROM transactions WHERE pending_sync = 1) + (SELECT COUNT(*) FROM budgets WHERE pending_sync = 1)
           + (SELECT COUNT(*) FROM manual_rates WHERE pending_sync = 1)""",
    )
    abstract fun observePendingCount(): Flow<Int>

    @Transaction
    open suspend fun pending() = PendingRows(
        pendingAccounts(), pendingCategories(), pendingTransactions(), pendingBudgets(), pendingManualRates(),
    )

    // --- clear the flag only if the row was not edited again while it was being sent ---
    @Query("UPDATE accounts SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearAccount(id: String, updatedAt: Long)
    @Query("UPDATE categories SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearCategory(id: String, updatedAt: Long)
    @Query("UPDATE transactions SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearTransaction(id: String, updatedAt: Long)
    @Query("UPDATE budgets SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearBudget(id: String, updatedAt: Long)
    @Query("UPDATE manual_rates SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearManualRate(id: String, updatedAt: Long)

    // --- local versions of incoming rows (including deleted) ---
    @Query("SELECT * FROM accounts WHERE id IN (:ids)") abstract suspend fun accounts(ids: List<String>): List<AccountEntity>
    @Query("SELECT * FROM categories WHERE id IN (:ids)") abstract suspend fun categories(ids: List<String>): List<CategoryEntity>
    @Query("SELECT * FROM transactions WHERE id IN (:ids)") abstract suspend fun transactions(ids: List<String>): List<TransactionEntity>
    @Query("SELECT * FROM budgets WHERE id IN (:ids)") abstract suspend fun budgets(ids: List<String>): List<BudgetEntity>
    @Query("SELECT * FROM manual_rates WHERE id IN (:ids)") abstract suspend fun manualRates(ids: List<String>): List<ManualRateEntity>

    @Upsert abstract suspend fun upsertAccounts(rows: List<AccountEntity>)
    @Upsert abstract suspend fun upsertCategories(rows: List<CategoryEntity>)
    @Upsert abstract suspend fun upsertTransactions(rows: List<TransactionEntity>)
    @Upsert abstract suspend fun upsertBudgets(rows: List<BudgetEntity>)
    @Upsert abstract suspend fun upsertManualRates(rows: List<ManualRateEntity>)

    @Query("SELECT COUNT(*) FROM transactions WHERE deleted = 0") abstract suspend fun liveTransactionCount(): Int

    // --- state ---
    @Query("SELECT value FROM sync_state WHERE `key` = :key") abstract suspend fun state(key: String): String?
    @Query("SELECT value FROM sync_state WHERE `key` = :key") abstract fun observeState(key: String): Flow<String?>
    @Upsert abstract suspend fun setState(row: SyncStateEntity)

    /**
     * Applies downloaded rows that win over local ones ([remoteWins]) and moves the cursor, in one
     * transaction: a local edit saved during the merge is either seen here or made after it.
     * Returns how many rows changed.
     */
    @Transaction
    open suspend fun merge(rows: PulledRows, cursorKey: String, cursor: Long?): Int {
        var changed = 0
        fun <T> winners(remote: List<T>, local: List<T>, id: (T) -> String, v: (T) -> Version): List<T> {
            val byId = local.associateBy(id)
            return remote.filter { remoteWins(byId[id(it)]?.let(v), v(it)) }.also { changed += it.size }
        }
        upsertAccounts(winners(rows.accounts, accounts(rows.accounts.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        upsertCategories(winners(rows.categories, categories(rows.categories.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        upsertTransactions(winners(rows.transactions, transactions(rows.transactions.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        upsertBudgets(winners(rows.budgets, budgets(rows.budgets.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        upsertManualRates(winners(rows.manualRates, manualRates(rows.manualRates.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        if (cursor != null) setState(SyncStateEntity(cursorKey, cursor.toString()))
        return changed
    }

    // --- replace local data with the family's (joining a family) ---
    @Query("DELETE FROM accounts") abstract suspend fun deleteAccounts()
    @Query("DELETE FROM categories") abstract suspend fun deleteCategories()
    @Query("DELETE FROM transactions") abstract suspend fun deleteTransactions()
    @Query("DELETE FROM budgets") abstract suspend fun deleteBudgets()
    @Query("DELETE FROM manual_rates") abstract suspend fun deleteManualRates()
    @Query("DELETE FROM sync_state") abstract suspend fun deleteState()

    @Transaction
    open suspend fun wipeSyncedData() {
        deleteAccounts(); deleteCategories(); deleteTransactions(); deleteBudgets(); deleteManualRates(); deleteState()
    }

    /** Marks every row as not yet sent — used when this phone's data must go to a (new) family. */
    @Query("UPDATE accounts SET pending_sync = 1") abstract suspend fun markAccountsPending()
    @Query("UPDATE categories SET pending_sync = 1") abstract suspend fun markCategoriesPending()
    @Query("UPDATE transactions SET pending_sync = 1") abstract suspend fun markTransactionsPending()
    @Query("UPDATE budgets SET pending_sync = 1") abstract suspend fun markBudgetsPending()
    @Query("UPDATE manual_rates SET pending_sync = 1") abstract suspend fun markManualRatesPending()

    @Transaction
    open suspend fun markAllPending() {
        markAccountsPending(); markCategoriesPending(); markTransactionsPending(); markBudgetsPending(); markManualRatesPending()
        deleteState()
    }
}

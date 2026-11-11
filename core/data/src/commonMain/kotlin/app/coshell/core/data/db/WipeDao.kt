package app.coshell.core.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

/**
 * «Стереть все данные» (feedback #107): tombstones every live row of the synced tables, so the wipe
 * reaches the other phone by the usual sync. Downloaded rates and `sync_state` stay.
 */
@Dao
interface WipeDao {
    @Query("SELECT COUNT(*) FROM accounts WHERE deleted = 0") suspend fun accounts(): Int
    @Query("SELECT COUNT(*) FROM transactions WHERE deleted = 0") suspend fun transactions(): Int
    @Query("SELECT COUNT(*) FROM categories WHERE deleted = 0") suspend fun categories(): Int
    @Query("SELECT COUNT(*) FROM manual_rates WHERE deleted = 0") suspend fun manualRates(): Int

    @Query("UPDATE accounts SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE deleted = 0")
    suspend fun deleteAccounts(now: Long, by: String?)

    @Query("UPDATE categories SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE deleted = 0")
    suspend fun deleteCategories(now: Long, by: String?)

    @Query("UPDATE budgets SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE deleted = 0")
    suspend fun deleteBudgets(now: Long, by: String?)

    @Query("UPDATE manual_rates SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE deleted = 0")
    suspend fun deleteManualRates(now: Long, by: String?)

    @Query("UPDATE templates SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE deleted = 0")
    suspend fun deleteTemplates(now: Long, by: String?)

    /** Also frees the import ids (deleted rows included): a fresh Wallet import must bring everything back. */
    @Query(
        """UPDATE transactions SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by,
           import_source = NULL, import_external_id = NULL, import_batch_id = NULL
           WHERE deleted = 0 OR import_external_id IS NOT NULL""",
    )
    suspend fun deleteTransactions(now: Long, by: String?)

    @Transaction
    suspend fun wipeAll(now: Long, by: String?) {
        deleteTransactions(now, by)
        deleteBudgets(now, by)
        deleteTemplates(now, by)
        deleteManualRates(now, by)
        deleteCategories(now, by)
        deleteAccounts(now, by)
    }
}

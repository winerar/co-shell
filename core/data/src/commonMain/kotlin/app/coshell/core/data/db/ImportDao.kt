package app.coshell.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** One import batch as an aggregate over its live operations. */
data class ImportBatchRow(
    @ColumnInfo(name = "import_batch_id") val batchId: String,
    @ColumnInfo(name = "import_source") val source: String,
    val count: Int,
    @ColumnInfo(name = "first_date") val firstDate: Long,
    @ColumnInfo(name = "last_date") val lastDate: Long,
    @ColumnInfo(name = "imported_at") val importedAt: Long,
)

/** Importers (stage 5c): atomic batch writes and undo. */
@Dao
interface ImportDao {
    /** Deleted rows count too: their ids are still taken by the unique index. */
    @Query("SELECT import_external_id FROM transactions WHERE import_source = :source AND import_external_id IS NOT NULL")
    suspend fun externalIds(source: String): List<String>

    @Upsert suspend fun upsertAccounts(entities: List<AccountEntity>)
    @Upsert suspend fun upsertCategories(entities: List<CategoryEntity>)
    @Upsert suspend fun upsertTransactions(entities: List<TransactionEntity>)

    @Transaction
    suspend fun save(accounts: List<AccountEntity>, categories: List<CategoryEntity>, transactions: List<TransactionEntity>) {
        upsertAccounts(accounts)
        upsertCategories(categories)
        upsertTransactions(transactions)
    }

    @Query(
        """SELECT import_batch_id, import_source, COUNT(*) AS count, MIN(date) AS first_date,
           MAX(date) AS last_date, MIN(updated_at) AS imported_at
           FROM transactions WHERE deleted = 0 AND import_batch_id IS NOT NULL
           GROUP BY import_batch_id ORDER BY imported_at DESC""",
    )
    fun observeBatches(): Flow<List<ImportBatchRow>>

    /**
     * Tombstones the batch and frees its external ids, so importing the same file again brings the
     * records back (the unique index would otherwise silently drop them).
     */
    @Query(
        """UPDATE transactions SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by,
           import_source = NULL, import_external_id = NULL, import_batch_id = NULL
           WHERE import_batch_id = :batchId AND deleted = 0""",
    )
    suspend fun undoBatch(batchId: String, now: Long, by: String?): Int
}

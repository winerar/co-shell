package app.coshell.core.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RateDao {
    @Query("SELECT * FROM manual_rates WHERE deleted = 0 ORDER BY date DESC, id")
    fun observeManual(): Flow<List<ManualRateEntity>>

    @Query("SELECT * FROM manual_rates WHERE deleted = 0")
    suspend fun getAllManual(): List<ManualRateEntity>

    @Upsert
    suspend fun upsertManual(entities: List<ManualRateEntity>)

    @Query("UPDATE manual_rates SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE id = :id AND deleted = 0")
    suspend fun markManualDeleted(id: String, now: Long, by: String?)

    @Query("SELECT * FROM downloaded_rates")
    fun observeDownloaded(): Flow<List<DownloadedRateEntity>>

    @Upsert
    suspend fun upsertDownloaded(entities: List<DownloadedRateEntity>)

    @Query("SELECT MAX(date) FROM downloaded_rates")
    suspend fun latestDownloadedDate(): String?

    /** Every currency live data is kept in; NULLs of optional columns are dropped. */
    @Query(
        """SELECT currency FROM accounts WHERE deleted = 0
           UNION SELECT currency FROM transactions WHERE deleted = 0
           UNION SELECT to_currency FROM transactions WHERE deleted = 0 AND to_currency IS NOT NULL
           UNION SELECT original_currency FROM transactions WHERE deleted = 0 AND original_currency IS NOT NULL
           UNION SELECT currency FROM budgets WHERE deleted = 0""",
    )
    suspend fun usedCurrencies(): List<String>
}

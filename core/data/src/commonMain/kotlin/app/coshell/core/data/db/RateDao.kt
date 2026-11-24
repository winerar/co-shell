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

    /** Newest downloaded row (latest date, then latest download) — for "last updated" on the rates screen. */
    @Query("SELECT * FROM downloaded_rates ORDER BY date DESC, downloaded_at DESC LIMIT 1")
    fun observeLatestDownloaded(): Flow<DownloadedRateEntity?>

    @Query("SELECT MAX(date) FROM downloaded_rates")
    suspend fun latestDownloadedDate(): String?

    @Query("SELECT DISTINCT date FROM downloaded_rates")
    suspend fun downloadedDates(): List<String>

    /** Epoch millis of live operations; days are computed in Kotlin (time zone). */
    @Query("SELECT DISTINCT date FROM transactions WHERE deleted = 0")
    suspend fun operationTimes(): List<Long>

    /**
     * Every currency live data is kept in; NULLs of optional columns are dropped. Archived accounts and their operations
     * do not count (#604): a currency left only on an archived account needs no new rates (old ones stay for history).
     */
    @Query(
        """SELECT currency FROM accounts WHERE deleted = 0 AND archived = 0
           UNION SELECT currency FROM transactions WHERE deleted = 0 AND account_id NOT IN ($ARCHIVED)
           UNION SELECT to_currency FROM transactions WHERE deleted = 0 AND to_currency IS NOT NULL
               AND (to_account_id IS NULL OR to_account_id NOT IN ($ARCHIVED))
           UNION SELECT original_currency FROM transactions WHERE deleted = 0 AND original_currency IS NOT NULL
               AND account_id NOT IN ($ARCHIVED)
           UNION SELECT currency FROM budgets WHERE deleted = 0
           UNION SELECT currency FROM templates WHERE deleted = 0 AND amount_minor IS NOT NULL""",
    )
    suspend fun usedCurrencies(): List<String>
}

private const val ARCHIVED = "SELECT id FROM accounts WHERE archived = 1"

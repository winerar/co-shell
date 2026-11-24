package app.coshell.core.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Events of debts (#552). Screens see live rows; sync and the backup read through [SyncDao] / [getAll]. */
@Dao
interface DebtDao {
    @Query("SELECT * FROM debt_events WHERE deleted = 0 ORDER BY date, id")
    fun observe(): Flow<List<DebtEventEntity>>

    @Query("SELECT * FROM debt_events WHERE deleted = 0 ORDER BY date, id")
    suspend fun getAll(): List<DebtEventEntity>

    @Upsert
    suspend fun upsert(entities: List<DebtEventEntity>)

    @Query("UPDATE debt_events SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE debt_id = :debtId AND deleted = 0")
    suspend fun markDebtDeleted(debtId: String, now: Long, by: String?)

    @Query("UPDATE debt_events SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE id = :id AND deleted = 0")
    suspend fun markDeleted(id: String, now: Long, by: String?)
}

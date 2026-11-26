package app.coshell.core.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Savings goals (#768). Screens see live rows; sync and the backup read through [SyncDao] / [getAll]. */
@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE deleted = 0 ORDER BY name COLLATE NOCASE, id")
    fun observe(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE deleted = 0 ORDER BY name COLLATE NOCASE, id")
    suspend fun getAll(): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE id = :id AND deleted = 0")
    suspend fun get(id: String): GoalEntity?

    @Upsert
    suspend fun upsert(entities: List<GoalEntity>)

    @Query("UPDATE goals SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE id = :id AND deleted = 0")
    suspend fun markDeleted(id: String, now: Long, by: String?)
}

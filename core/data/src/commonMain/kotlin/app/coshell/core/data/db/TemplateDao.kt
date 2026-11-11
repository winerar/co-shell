package app.coshell.core.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates WHERE deleted = 0 ORDER BY sort_order, name")
    fun observe(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE id = :id AND deleted = 0")
    suspend fun get(id: String): TemplateEntity?

    @Query("SELECT * FROM templates WHERE deleted = 0")
    suspend fun getAll(): List<TemplateEntity>

    @Query("SELECT MAX(sort_order) FROM templates WHERE deleted = 0")
    suspend fun maxSortOrder(): Int?

    @Upsert
    suspend fun upsert(entities: List<TemplateEntity>)

    @Query("UPDATE templates SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long, by: String?)
}

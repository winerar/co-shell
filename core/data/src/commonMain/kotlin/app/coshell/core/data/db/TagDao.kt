package app.coshell.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Tags (#129). Like the other screen DAOs, reads skip tombstones — except [find], which must see them. */
@Dao
interface TagDao {
    @Query("SELECT * FROM tags WHERE deleted = 0 ORDER BY name COLLATE NOCASE")
    fun observe(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE deleted = 0")
    suspend fun getAll(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE id = :id AND deleted = 0")
    suspend fun get(id: String): TagEntity?

    /** The row with [id], deleted or not: a free id for a new tag is one with no row at all. */
    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun find(id: String): TagEntity?

    @Upsert
    suspend fun upsert(entities: List<TagEntity>)

    /** Writes only rows whose id has no row yet (deleted rows count as present). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entities: List<TagEntity>)

    @Query("UPDATE tags SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long, by: String?)

    /** Tag references and dates of live operations that have any (`labels` is a JSON array, `[]` — none). */
    @Query("SELECT labels, date FROM transactions WHERE deleted = 0 AND labels != '[]'")
    fun observeLabelUses(): Flow<List<LabelUse>>
}

/** `labels` of one operation and its date (epoch ms) — for tag counts and «recent». */
data class LabelUse(val labels: String, val date: Long)

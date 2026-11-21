package app.coshell.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Drafts of unfinished expenses from quick notes (schema v17, #586). **Device-only**: no sync fields, not in the backup,
 * not sent to the other phone; hard-deleted (no tombstones). Emptied by «Стереть всё» ([WipeDao.wipeAll]).
 */
@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey val id: String,
    val title: String,
    val currency: String,
    /** `NULL` — the line had no number. */
    @ColumnInfo(name = "amount_minor") val amountMinor: Long?,
    @ColumnInfo(name = "account_id") val accountId: String?,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    /** `DraftCategorySource` name, `NULL` — no category or chosen by the person. */
    @ColumnInfo(name = "category_source") val categorySource: String?,
    /** Epoch ms UTC of the expense's day. */
    val date: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    val origin: String,
)

@Dao
interface DraftDao {
    /** Oldest first, ties by id: the order the lines were written in. */
    @Query("SELECT * FROM drafts ORDER BY created_at, id")
    fun observe(): Flow<List<DraftEntity>>

    @Query("SELECT * FROM drafts WHERE id = :id")
    suspend fun get(id: String): DraftEntity?

    @Upsert
    suspend fun upsert(entities: List<DraftEntity>)

    @Query("DELETE FROM drafts WHERE id IN (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("DELETE FROM drafts")
    suspend fun clear()

    /** One write for the whole note: the list never shows half of it. */
    @Transaction
    suspend fun replaceAll(entities: List<DraftEntity>) = upsert(entities)
}

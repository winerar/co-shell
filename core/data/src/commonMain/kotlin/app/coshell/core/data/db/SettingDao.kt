package app.coshell.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Query("SELECT value FROM settings WHERE id = :id AND deleted = 0")
    fun observe(id: String): Flow<String?>

    @Query("SELECT * FROM settings WHERE deleted = 0")
    suspend fun getAll(): List<SettingEntity>

    @Upsert
    suspend fun upsert(entities: List<SettingEntity>)

    /** Writes [entity] only when there is no row with its key yet (deleted rows count as present). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entity: SettingEntity)
}

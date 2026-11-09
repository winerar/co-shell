package app.coshell.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Small key-value table for sync bookkeeping (schema v4), kept in the DB so it is reset with the data. */
@Entity(tableName = "sync_state")
data class SyncStateEntity(@PrimaryKey val key: String, val value: String)

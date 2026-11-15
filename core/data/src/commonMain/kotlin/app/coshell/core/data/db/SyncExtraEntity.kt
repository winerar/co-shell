package app.coshell.core.data.db

import androidx.room.Entity

/**
 * Fields of a synced row that this app version does not know (#340): a newer version on the other
 * phone added them. [json] is a JSON object with only those keys; it is merged back into the row's
 * payload on every push, so an edit made here does not erase them. [kind] is a `SyncKind.wire` name.
 * Not synced itself; rows without unknown fields have no entry (schema v12).
 */
@Entity(tableName = "sync_extras", primaryKeys = ["kind", "id"])
data class SyncExtraEntity(val kind: String, val id: String, val json: String)

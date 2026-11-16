package app.coshell.core.data.sync

/**
 * The other phone runs a newer, incompatible version (#334, board `Fb334A`): a pulled record carries a
 * sync schema ([SyncPayload.KEY]) above this version's [SyncPayload.SCHEMA]. Looking stays open, entry
 * closes until the update — this version would misread or half-write data it does not understand.
 *
 * [SyncEngine] keeps the highest schema it has pulled under [KEY] in `sync_state`; the gate opens by
 * itself once the update brings a [SyncPayload.SCHEMA] that high.
 */
object VersionGate {
    /** Key in `sync_state`: the highest sync schema seen in pulled records. */
    const val KEY = "peer_schema"

    /** Entry is closed: some pulled record needs a newer version than this one. */
    fun isLocked(stored: String?): Boolean = (stored?.toIntOrNull() ?: 0) > SyncPayload.SCHEMA

    /** The value to store after a pull that saw [seen] (`null` — nothing pulled); `null` — no change. */
    fun raised(stored: String?, seen: Int?): String? {
        if (seen == null) return null
        val before = stored?.toIntOrNull() ?: 0
        return if (seen > before) seen.toString() else null
    }
}

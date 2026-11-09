package app.coshell.core.data.sync

/** Version of a row for conflict resolution. */
data class Version(val updatedAt: Long, val updatedBy: String?)

/**
 * Last-write-wins by the time of the edit on the phone (`updatedAt`), not by arrival at the
 * server: an edit made offline yesterday must not overwrite today's edit from the other phone.
 * Equal times (practically never) are broken by member id so both phones pick the same winner.
 * Returns true when the incoming [remote] version should replace [local] (`null` — no local row).
 */
fun remoteWins(local: Version?, remote: Version): Boolean {
    if (local == null) return true
    if (remote.updatedAt != local.updatedAt) return remote.updatedAt > local.updatedAt
    return (remote.updatedBy ?: "") > (local.updatedBy ?: "")
}

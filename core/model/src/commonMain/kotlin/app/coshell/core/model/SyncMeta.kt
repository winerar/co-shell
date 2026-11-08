package app.coshell.core.model

import kotlin.time.Instant

/**
 * Service fields carried by every synchronized entity (ARCHITECTURE §5).
 *
 * Conflicts are resolved last-write-wins by [updatedAt]. Entities are never physically
 * removed while sync is possible: deletion sets [deleted] so the other phone learns about it.
 */
data class SyncMeta(
    val updatedAt: Instant,
    val updatedBy: MemberId? = null,
    val deleted: Boolean = false,
)

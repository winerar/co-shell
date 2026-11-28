package app.coshell.core.data.sync

import app.coshell.core.data.db.TemplateEntity
import app.coshell.core.data.repository.ScheduleCodec

/**
 * «Проведено по» of a regular payment (#406) only grows (`handledUpTo`), but a template syncs whole by last write:
 * an edit on another phone made before it saw the payment would move the date back and show the payment as missed again.
 * [SyncDao.merge] writes templates through [rowsToWrite], which keeps the later date on both sides.
 */
internal object TemplateHandledMerge {
    /**
     * Rows to write: the remote rows that [won], each with the later date of the two, and local rows that kept
     * their place but held an earlier date than the remote one. A changed row is pending: the family's copy catches up.
     */
    fun rowsToWrite(won: List<TemplateEntity>, remote: List<TemplateEntity>, local: List<TemplateEntity>): List<TemplateEntity> {
        val localById = local.associateBy { it.id }
        val wonIds = won.mapTo(HashSet()) { it.id }
        val winners = won.map { it.withLaterHandled(localById[it.id]) }
        val raised = remote.filter { it.id !in wonIds }.mapNotNull { r ->
            localById[r.id]?.let { l -> l.withLaterHandled(r).takeIf { it !== l } }
        }
        return winners + raised
    }

    /** [winner] schedule with the later «проведено по» of [winner] and [other]; unreadable or no repeat — as is. */
    fun laterHandled(winner: String?, other: String?): String? {
        val w = ScheduleCodec.decode(winner) ?: return winner
        val theirs = ScheduleCodec.decode(other)?.handledThrough ?: return winner
        val mine = w.handledThrough
        return if (mine != null && mine >= theirs) winner else ScheduleCodec.encode(w.copy(handledThrough = theirs))
    }

    private fun TemplateEntity.withLaterHandled(other: TemplateEntity?): TemplateEntity {
        if (other == null || deleted || other.deleted) return this
        val merged = laterHandled(schedule, other.schedule)
        return if (merged == schedule) this else copy(schedule = merged, pendingSync = true)
    }
}

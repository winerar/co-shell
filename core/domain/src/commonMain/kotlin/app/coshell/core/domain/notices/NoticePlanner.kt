package app.coshell.core.domain.notices

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.daysUntil

/** What the daily check knows about the data. Dates are the phone's calendar days. */
data class NoticeFacts(
    val today: LocalDate,
    /** At least one live entry exists. */
    val hasEntries: Boolean,
    val entryToday: Boolean,
    val lastEntryDay: LocalDate?,
    /** The first calendar day the app was seen on this phone (backup baseline when no copy was ever made). */
    val firstSeenDay: LocalDate,
    /** The last automatic copy; `null` — never (or the copies are off). */
    val lastBackupDay: LocalDate?,
    /** The limit worth telling about (`LimitAlerts`): 80–99 % spent, else spending ahead of the month. */
    val limit: LimitFact? = null,
    /** The last full month had entries — there is something to recap. */
    val lastMonthHadEntries: Boolean = false,
    /** Drafts of quick notes with an amount, an account and a category (`DraftCheck`). */
    val readyDrafts: Int = 0,
)

/**
 * The limit the notification names: «[name]: ещё можно [left]», «это [percentLeft] % лимита на [daysLeft] дн.»; [pace] (#186) —
 * not yet 80 %, but spending runs ahead of the month: «[name]: траты опережают месяц». [key] changes with the limit, the month and
 * the kind (`LimitAlert.key`), so the same case is told once.
 */
data class LimitFact(
    val key: String,
    val name: String,
    val left: String,
    val percentLeft: Int = 0,
    val daysLeft: Int = 0,
    val pace: Boolean = false,
    /** The envelope a tap opens: the limit's category and its month (`2026-11`). */
    val categoryId: String = "",
    val month: String = "",
)

/** What went out before: the day of the last optional notice and, per kind, its day and key. Phone-only. */
data class NoticeHistory(
    val lastAnyDay: LocalDate? = null,
    val perKind: Map<NoticeKind, Shown> = emptyMap(),
) {
    data class Shown(val day: LocalDate, val key: String)
}

/** The notice to post now and the key to remember. [limit] is set only for [NoticeKind.LIMIT]. */
data class PlannedNotice(val kind: NoticeKind, val key: String, val limit: LimitFact? = null)

/**
 * Which optional notification goes out in today's check (#745, `hints.md` §«Уведомления»). Pure: the worker collects the facts.
 * Rules: only kinds that are on; **at most one a day**; each kind says its own conditions and never repeats the same case.
 * No tone here — texts live in resources.
 */
object NoticePlanner {
    const val AWAY_DAYS = 3
    const val BACKUP_AFTER_DAYS = 14
    const val DRAFTS_MIN = 3
    const val DRAFTS_AGAIN_DAYS = 3
    const val RECAP_UNTIL_DAY = 3

    fun next(facts: NoticeFacts, enabled: Set<NoticeKind>, history: NoticeHistory): PlannedNotice? {
        if (history.lastAnyDay == facts.today) return null
        for (kind in NoticeKind.entries) {
            if (kind !in enabled) continue
            val planned = candidate(kind, facts, history) ?: continue
            if (history.perKind[kind]?.key == planned.key && kind != NoticeKind.EVENING) continue
            return planned
        }
        return null
    }

    private fun candidate(kind: NoticeKind, f: NoticeFacts, h: NoticeHistory): PlannedNotice? = when (kind) {
        NoticeKind.LIMIT -> f.limit?.let { PlannedNotice(kind, it.key, it) }
        NoticeKind.RECAP ->
            if (f.hasEntries && f.lastMonthHadEntries && f.today.day <= RECAP_UNTIL_DAY) {
                PlannedNotice(kind, f.today.minus(DatePeriod(months = 1)).let { "${it.year}-${it.monthNumber}" })
            } else null
        NoticeKind.COMEBACK ->
            if (f.hasEntries && f.lastEntryDay != null && f.lastEntryDay.daysUntil(f.today) >= AWAY_DAYS) PlannedNotice(kind, f.lastEntryDay.toString()) else null
        NoticeKind.BACKUP -> {
            val since = listOfNotNull(f.lastBackupDay, f.firstSeenDay).max()
            val again = h.perKind[kind]?.day?.let { it.daysUntil(f.today) >= BACKUP_AFTER_DAYS } ?: true
            if (f.hasEntries && since.daysUntil(f.today) >= BACKUP_AFTER_DAYS && again) PlannedNotice(kind, f.today.toString()) else null
        }
        NoticeKind.DRAFTS -> {
            val again = h.perKind[kind]?.day?.let { it.daysUntil(f.today) >= DRAFTS_AGAIN_DAYS } ?: true
            if (f.readyDrafts >= DRAFTS_MIN && again) PlannedNotice(kind, f.today.toString()) else null
        }
        NoticeKind.EVENING -> if (f.hasEntries && !f.entryToday) PlannedNotice(kind, f.today.toString()) else null
    }
}

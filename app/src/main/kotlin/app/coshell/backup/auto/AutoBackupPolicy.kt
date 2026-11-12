package app.coshell.backup.auto

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Rules of the weekly automatic copy; pure functions, unit-tested. */
object AutoBackupPolicy {
    /** How many automatic copies stay in the folder; older ones are deleted (manual copies are never touched). */
    const val KEEP = 8
    const val PERIOD_DAYS = 7L
    private const val PREFIX = "co-shell-auto-"

    fun fileName(date: LocalDate): String = "$PREFIX$date.json"

    fun isAutoCopy(name: String): Boolean = name.startsWith(PREFIX) && name.endsWith(".json")

    /** Names of automatic copies to delete so that [KEEP] newest remain (names sort by date). */
    fun toPrune(names: List<String>): List<String> =
        names.filter(::isAutoCopy).sortedDescending().drop(KEEP)

    /**
     * Whole days until the next weekly copy (0 — due today or overdue), `null` — no copy yet.
     * The worker runs once a week after the last copy, so the countdown starts from its date.
     */
    fun daysUntilNext(last: LocalDate?, today: LocalDate): Int? {
        last ?: return null
        val passed = last.daysUntil(today).coerceAtLeast(0)
        return (PERIOD_DAYS - passed).coerceIn(0, PERIOD_DAYS).toInt()
    }

    /** How full the countdown ring is: 0 right after a copy, 1 when the next one is due. */
    fun ringFraction(daysLeft: Int): Float = 1f - daysLeft.toFloat() / PERIOD_DAYS
}

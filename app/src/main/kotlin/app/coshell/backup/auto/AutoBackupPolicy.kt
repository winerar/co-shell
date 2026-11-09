package app.coshell.backup.auto

import kotlinx.datetime.LocalDate

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
}

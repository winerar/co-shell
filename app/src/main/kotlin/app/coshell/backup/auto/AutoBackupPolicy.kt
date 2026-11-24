package app.coshell.backup.auto

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/** Rules of the automatic copy (period, day, hour — [BackupSchedule]); pure functions, unit-tested. */
object AutoBackupPolicy {
    /** How many automatic copies stay in the folder; older ones are deleted (manual copies are never touched). */
    const val KEEP = 8
    private const val PREFIX = "co-shell-auto-"

    fun fileName(date: LocalDate): String = "$PREFIX$date.json"

    fun isAutoCopy(name: String): Boolean = name.startsWith(PREFIX) && name.endsWith(".json")

    /** Names of automatic copies to delete so that the [keep] newest remain (names sort by date). */
    fun toPrune(names: List<String>, keep: Int = KEEP): List<String> =
        names.filter(::isAutoCopy).sortedDescending().drop(keep)

    /** Is [date] a day of the schedule: any day, the chosen weekday, or the chosen day of the month (31 — the last one). */
    fun isScheduledDay(schedule: BackupSchedule, date: LocalDate): Boolean = when (schedule.period) {
        BackupPeriod.DAILY -> true
        BackupPeriod.WEEKLY -> date.dayOfWeek == schedule.weekday
        BackupPeriod.MONTHLY -> date.day == minOf(schedule.monthDay, daysInMonth(date))
    }

    /** The first moment of the schedule strictly after [after] (a scheduled day at the schedule's hour). */
    fun nextSlot(schedule: BackupSchedule, after: LocalDateTime): LocalDateTime {
        val time = LocalTime(schedule.hour.coerceIn(0, 23), 0)
        var date = after.date
        // A month is the longest gap: 62 days is more than enough.
        repeat(SEARCH_DAYS) {
            if (isScheduledDay(schedule, date)) {
                val slot = LocalDateTime(date, time)
                if (slot > after) return slot
            }
            date = date.plus(1, DateTimeUnit.DAY)
        }
        error("No slot within $SEARCH_DAYS days of $after")
    }

    /** Is a copy due: no copy yet, or a slot of the schedule has passed since the last one (overdue counts). */
    fun isDue(schedule: BackupSchedule, last: LocalDateTime?, now: LocalDateTime): Boolean =
        last == null || nextSlot(schedule, last) <= now

    /** The slot for «Следующая: …» — the nearest one from [now]. */
    fun upcoming(schedule: BackupSchedule, now: LocalDateTime): LocalDateTime = nextSlot(schedule, now)

    /**
     * Whole days until the next copy (0 — due today or overdue), `null` — no copy yet.
     * The countdown starts from the last copy and ends on the next slot after it.
     */
    fun daysUntilNext(schedule: BackupSchedule, last: LocalDateTime?, now: LocalDateTime): Int? {
        last ?: return null
        val next = nextSlot(schedule, last)
        if (next <= now) return 0
        return now.date.daysUntil(next.date).coerceAtLeast(0)
    }

    /** How full the countdown ring is: 0 right after a copy, 1 when the next one is due. */
    fun ringFraction(daysLeft: Int, period: BackupPeriod = BackupPeriod.WEEKLY): Float =
        (1f - daysLeft.toFloat() / period.days).coerceIn(0f, 1f)

    private fun daysInMonth(date: LocalDate): Int {
        val first = LocalDate(date.year, date.month, 1)
        return first.daysUntil(first.plus(DatePeriod(months = 1)))
    }

    private const val SEARCH_DAYS = 62
}

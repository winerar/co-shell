package app.coshell.recurring.notify

import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/** «Напомнить — в 9:00 в день платежа» (#143, board `Rec143Notify`). */
object ReminderTime {
    val AT: LocalTime = LocalTime(9, 0)

    /** From [now] to the next [at] (9:00 unless «Время напоминания» says otherwise): today's if it is still ahead, else tomorrow's. */
    fun untilNext(now: Instant, zone: TimeZone, at: LocalTime = AT): Duration {
        val today = now.toLocalDateTime(zone).date
        val todayAt = today.atTime(at).toInstant(zone)
        val next = if (now < todayAt) todayAt else today.plus(DatePeriod(days = 1)).atTime(at).toInstant(zone)
        return next - now
    }
}

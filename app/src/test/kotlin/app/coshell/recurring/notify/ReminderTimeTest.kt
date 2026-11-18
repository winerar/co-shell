package app.coshell.recurring.notify

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderTimeTest {
    private val ba = TimeZone.of("America/Argentina/Buenos_Aires") // UTC−3

    @Test
    fun `before nine waits for today's nine`() {
        assertEquals(2.hours + 30.minutes, ReminderTime.untilNext(Instant.parse("2026-11-15T09:30:00Z"), ba))
    }

    @Test
    fun `at or after nine waits for tomorrow`() {
        assertEquals(24.hours, ReminderTime.untilNext(Instant.parse("2026-11-15T12:00:00Z"), ba))
        assertEquals(13.hours, ReminderTime.untilNext(Instant.parse("2026-11-15T23:00:00Z"), ba))
    }

    @Test
    fun `a chosen hour is the next one on the phone clock`() {
        val at18 = LocalTime(18, 0)
        // 09:30 Buenos Aires = 12:30 UTC; 18:00 is 8 h 30 min ahead, today's.
        assertEquals(8.hours + 30.minutes, ReminderTime.untilNext(Instant.parse("2026-11-15T12:30:00Z"), ba, at18))
        // 19:00 local is past 18:00 — tomorrow's.
        assertEquals(23.hours, ReminderTime.untilNext(Instant.parse("2026-11-15T22:00:00Z"), ba, at18))
    }
}

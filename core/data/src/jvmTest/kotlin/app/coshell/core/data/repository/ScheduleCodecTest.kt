package app.coshell.core.data.repository

import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScheduleCodecTest {
    private val start = LocalDate(2026, 11, 1)

    @Test
    fun `every kind survives the text column`() {
        listOf(
            RecurringSchedule(Recurrence.Monthly(31), start),
            RecurringSchedule(Recurrence.Weekly(DayOfWeek.SUNDAY), start, PostMode.AUTO, LocalDate(2026, 11, 15)),
            RecurringSchedule(Recurrence.Yearly(2, 29), start),
        ).forEach { assertEquals(it, ScheduleCodec.decode(ScheduleCodec.encode(it))) }
        assertEquals("W7;2026-11-01;AUTO;2026-11-15", ScheduleCodec.encode(RecurringSchedule(Recurrence.Weekly(DayOfWeek.SUNDAY), start, PostMode.AUTO, LocalDate(2026, 11, 15))))
    }

    @Test
    fun `unreadable value from a newer app means no repeat`() {
        assertNull(ScheduleCodec.decode(null))
        assertNull(ScheduleCodec.decode("Q3;2026-11-01;REMIND;"))
        assertNull(ScheduleCodec.decode("M40;2026-11-01;REMIND;"))
        assertNull(ScheduleCodec.decode("M5;2026-11-01;LATER;"))
    }
}

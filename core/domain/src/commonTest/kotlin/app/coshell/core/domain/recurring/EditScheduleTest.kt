package app.coshell.core.domain.recurring

import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EditScheduleTest {
    private fun d(s: String) = LocalDate.parse(s)
    private val today = d("2026-11-15")

    @Test
    fun `none clears the rule`() {
        assertNull(RecurringSchedule(Recurrence.Monthly(5), d("2026-01-01")).edited(null, PostMode.AUTO, today))
        assertNull(null.edited(null, PostMode.REMIND, today))
    }

    @Test
    fun `a new rule starts today, so a payment due today shows at once`() {
        val s = null.edited(Recurrence.Monthly(15), PostMode.REMIND, today)!!
        assertEquals(today, s.start)
        assertNull(s.handledThrough)
        assertEquals(listOf(today), s.due(today))
    }

    @Test
    fun `the same rule with another mode keeps its dates`() {
        val old = RecurringSchedule(Recurrence.Monthly(5), d("2026-03-01"), PostMode.REMIND, handledThrough = d("2026-10-05"))
        assertEquals(old.copy(mode = PostMode.AUTO), old.edited(Recurrence.Monthly(5), PostMode.AUTO, today))
    }

    @Test
    fun `another day counts from the day after the last handled payment`() {
        val old = RecurringSchedule(Recurrence.Monthly(10), d("2026-03-01"), handledThrough = d("2026-10-10"))
        val moved = old.edited(Recurrence.Monthly(5), PostMode.REMIND, today)!!
        assertEquals(d("2026-10-11"), moved.start)
        // November's 5th is owed: missed, not lost.
        assertEquals(listOf(d("2026-11-05")), moved.due(today))
    }

    @Test
    fun `another frequency of a never handled rule starts today`() {
        val old = RecurringSchedule(Recurrence.Monthly(1), d("2026-01-01"))
        val weekly = old.edited(Recurrence.Weekly(kotlinx.datetime.DayOfWeek.MONDAY), PostMode.REMIND, today)!!
        assertEquals(today, weekly.start)
        assertEquals(d("2026-11-16"), weekly.nextUnhandled())
    }
}

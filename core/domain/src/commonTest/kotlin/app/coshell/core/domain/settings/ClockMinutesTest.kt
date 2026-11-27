package app.coshell.core.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClockMinutesTest {

    @Test
    fun `steps by a quarter of an hour`() {
        assertEquals(ClockMinutes.of(9, 15), ClockMinutes.step(ClockMinutes.of(9), 1))
        assertEquals(ClockMinutes.of(8, 45), ClockMinutes.step(ClockMinutes.of(9), -1))
        assertEquals(ClockMinutes.of(10), ClockMinutes.step(ClockMinutes.of(9), 4))
    }

    @Test
    fun `wraps around midnight both ways`() {
        assertEquals(0, ClockMinutes.step(ClockMinutes.of(23, 45), 1))
        assertEquals(ClockMinutes.of(23, 45), ClockMinutes.step(0, -1))
    }

    @Test
    fun `an off-grid time snaps to the nearest step in the pressed direction`() {
        assertEquals(ClockMinutes.of(9, 15), ClockMinutes.step(ClockMinutes.of(9, 7), 1))
        assertEquals(ClockMinutes.of(9), ClockMinutes.step(ClockMinutes.of(9, 7), -1))
    }

    @Test
    fun `splits into hour and minute and checks the range`() {
        val t = ClockMinutes.of(18, 30)
        assertEquals(18, ClockMinutes.hour(t))
        assertEquals(30, ClockMinutes.minute(t))
        assertTrue(ClockMinutes.isValid(0))
        assertFalse(ClockMinutes.isValid(ClockMinutes.DAY))
        assertFalse(ClockMinutes.isValid(-1))
    }

    @Test
    fun `days before stay between the same day and a month`() {
        assertEquals(1, ReminderSettings.stepDays(0, 1))
        assertEquals(0, ReminderSettings.stepDays(0, -1))
        assertEquals(ReminderSettings.MAX_DAYS_BEFORE, ReminderSettings.stepDays(ReminderSettings.MAX_DAYS_BEFORE, 1))
    }

    @Test fun `due from a little before the time until midnight`() {
        val at = ClockMinutes.of(21)
        assertTrue(ClockMinutes.isDue(ClockMinutes.of(20, 45), at))
        assertTrue(ClockMinutes.isDue(ClockMinutes.of(23, 59), at))
        assertFalse(ClockMinutes.isDue(ClockMinutes.of(20, 44), at))
        assertFalse(ClockMinutes.isDue(ClockMinutes.of(0, 30), at))
    }
}

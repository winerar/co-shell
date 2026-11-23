package app.coshell.backup.auto

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoBackupPolicyTest {
    @Test
    fun `file name carries the date`() {
        assertEquals("co-shell-auto-2026-11-09.json", AutoBackupPolicy.fileName(LocalDate(2026, 11, 9)))
    }

    @Test
    fun `keeps the newest automatic copies and never touches other files`() {
        val auto = (1..10).map { AutoBackupPolicy.fileName(LocalDate(2026, 1, it)) }
        val names = auto.shuffled() + listOf("co-shell-2025-01-01.json", "notes.txt")
        assertEquals(auto.take(2).sortedDescending(), AutoBackupPolicy.toPrune(names))
    }

    @Test
    fun `nothing to prune while there are few copies`() {
        assertEquals(emptyList<String>(), AutoBackupPolicy.toPrune(listOf(AutoBackupPolicy.fileName(LocalDate(2026, 1, 1)))))
    }

    private fun at(m: Int, d: Int, h: Int = 3, min: Int = 0) = LocalDateTime(2026, m, d, h, min)

    private val weekly = BackupSchedule(BackupPeriod.WEEKLY, DayOfWeek.SUNDAY, hour = 3)

    @Test
    fun `default schedule is the old weekly copy with 8 kept`() {
        val default = BackupSchedule()
        assertEquals(BackupPeriod.WEEKLY, default.period)
        assertEquals(AutoBackupPolicy.KEEP, default.keep)
        assertTrue(AutoBackupPolicy.KEEP in BackupSchedule.KEEP_OPTIONS)
    }

    @Test
    fun `prune keeps as many as asked`() {
        val auto = (1..10).map { AutoBackupPolicy.fileName(LocalDate(2026, 1, it)) }
        assertEquals(7, AutoBackupPolicy.toPrune(auto, keep = 3).size)
    }

    @Test
    fun `daily slot is the next hour mark`() {
        val daily = BackupSchedule(BackupPeriod.DAILY, hour = 3)
        assertEquals(at(11, 4), AutoBackupPolicy.nextSlot(daily, at(11, 3, 3, 5)))
        assertEquals(at(11, 3), AutoBackupPolicy.nextSlot(daily, at(11, 3, 1)))
    }

    @Test
    fun `weekly slot is the chosen weekday`() {
        // 2026-11-23 is a Monday, 2026-11-29 a Sunday.
        assertEquals(at(11, 29), AutoBackupPolicy.nextSlot(weekly, at(11, 23, 12)))
        assertEquals(at(12, 6), AutoBackupPolicy.nextSlot(weekly, at(11, 29, 3)))
        val monday = weekly.copy(weekday = DayOfWeek.MONDAY, hour = 22)
        assertEquals(at(11, 23, 22), AutoBackupPolicy.nextSlot(monday, at(11, 23, 12)))
    }

    @Test
    fun `monthly slot is the chosen day and 31 means the last day`() {
        val first = BackupSchedule(BackupPeriod.MONTHLY, monthDay = 15)
        assertEquals(at(12, 15), AutoBackupPolicy.nextSlot(first, at(11, 15, 3)))
        val last = BackupSchedule(BackupPeriod.MONTHLY, monthDay = 31)
        assertEquals(at(11, 30), AutoBackupPolicy.nextSlot(last, at(11, 1)))
        assertEquals(LocalDateTime(2027, 2, 28, 3, 0), AutoBackupPolicy.nextSlot(last, LocalDateTime(2027, 2, 1, 0, 0)))
        assertEquals(LocalDateTime(2028, 2, 29, 3, 0), AutoBackupPolicy.nextSlot(last, LocalDateTime(2028, 2, 1, 0, 0)))
        assertEquals(at(12, 31), AutoBackupPolicy.nextSlot(last, at(11, 30, 4)))
    }

    @Test
    fun `due once a slot has passed since the last copy`() {
        val last = at(11, 22, 10) // 2026-11-22 is a Sunday: copied at 10:00, after that day's slot
        assertFalse(AutoBackupPolicy.isDue(weekly, at(11, 22, 10), at(11, 28, 23)))
        assertTrue(AutoBackupPolicy.isDue(weekly, last, at(11, 29, 3)))
        assertFalse(AutoBackupPolicy.isDue(weekly, last, at(11, 29, 2)))
    }

    @Test
    fun `overdue and never copied are due`() {
        assertTrue(AutoBackupPolicy.isDue(weekly, at(10, 1), at(11, 23)))
        assertTrue(AutoBackupPolicy.isDue(weekly, null, at(11, 23)))
    }

    @Test
    fun `a new weekday picked after a copy is due when it comes`() {
        val monday = weekly.copy(weekday = DayOfWeek.MONDAY)
        // last copy Sunday 22nd 03:05; Monday 23rd 03:00 is the next slot.
        assertTrue(AutoBackupPolicy.isDue(monday, at(11, 22, 3, 5), at(11, 23, 3)))
        assertFalse(AutoBackupPolicy.isDue(monday, at(11, 23, 3, 5), at(11, 25)))
    }

    @Test
    fun `counts days to the next copy`() {
        val last = at(11, 22, 3, 5)
        assertEquals(7, AutoBackupPolicy.daysUntilNext(weekly, last, at(11, 22, 4)))
        assertEquals(2, AutoBackupPolicy.daysUntilNext(weekly, last, at(11, 27, 4)))
        assertEquals(0, AutoBackupPolicy.daysUntilNext(weekly, last, at(11, 29, 4)))
        assertEquals(0, AutoBackupPolicy.daysUntilNext(weekly, last, at(12, 20)))
    }

    @Test
    fun `no copy yet means no countdown`() {
        assertEquals(null, AutoBackupPolicy.daysUntilNext(weekly, null, at(11, 3)))
    }

    @Test
    fun `upcoming slot is the nearest from now`() {
        assertEquals(at(11, 29), AutoBackupPolicy.upcoming(weekly, at(11, 23)))
    }

    @Test
    fun `ring fills as the next copy nears`() {
        assertEquals(0f, AutoBackupPolicy.ringFraction(7), 0.001f)
        assertEquals(5f / 7, AutoBackupPolicy.ringFraction(2), 0.001f)
        assertEquals(1f, AutoBackupPolicy.ringFraction(0), 0.001f)
        assertEquals(0f, AutoBackupPolicy.ringFraction(1, BackupPeriod.DAILY), 0.001f)
        assertEquals(1f - 10f / 31, AutoBackupPolicy.ringFraction(10, BackupPeriod.MONTHLY), 0.001f)
    }
}

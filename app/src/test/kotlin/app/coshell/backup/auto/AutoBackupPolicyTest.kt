package app.coshell.backup.auto

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
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

    @Test
    fun `counts days to the next weekly copy`() {
        val last = LocalDate(2026, 11, 3)
        assertEquals(7, AutoBackupPolicy.daysUntilNext(last, last))
        assertEquals(2, AutoBackupPolicy.daysUntilNext(last, LocalDate(2026, 11, 8)))
        assertEquals(0, AutoBackupPolicy.daysUntilNext(last, LocalDate(2026, 11, 10)))
    }

    @Test
    fun `an overdue copy is due today, a copy from the future counts a full week`() {
        val last = LocalDate(2026, 11, 3)
        assertEquals(0, AutoBackupPolicy.daysUntilNext(last, LocalDate(2026, 12, 1)))
        assertEquals(7, AutoBackupPolicy.daysUntilNext(last, LocalDate(2026, 11, 1)))
    }

    @Test
    fun `no copy yet means no countdown`() {
        assertEquals(null, AutoBackupPolicy.daysUntilNext(null, LocalDate(2026, 11, 3)))
    }

    @Test
    fun `ring fills as the next copy nears`() {
        assertEquals(0f, AutoBackupPolicy.ringFraction(7), 0.001f)
        assertEquals(5f / 7, AutoBackupPolicy.ringFraction(2), 0.001f)
        assertEquals(1f, AutoBackupPolicy.ringFraction(0), 0.001f)
    }
}

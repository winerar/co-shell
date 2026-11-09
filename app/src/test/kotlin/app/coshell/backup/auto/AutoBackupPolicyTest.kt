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
}

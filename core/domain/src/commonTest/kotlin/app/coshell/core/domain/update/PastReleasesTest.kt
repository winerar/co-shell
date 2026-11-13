package app.coshell.core.domain.update

import kotlin.test.Test
import kotlin.test.assertEquals

class PastReleasesTest {
    private fun rel(code: Int) = ReleaseNote(code, "0.$code", null, "• пункт $code")

    @Test
    fun keepsInstalledAndOlderNewestFirst() {
        val out = pastReleases(listOf(rel(10), rel(12), rel(13), rel(11)), installedVersionCode = 12)
        assertEquals(listOf(12, 11, 10), out.map { it.versionCode })
    }

    @Test
    fun limitsAndDropsDuplicates() {
        val history = (1..30).map(::rel) + rel(30)
        val out = pastReleases(history, installedVersionCode = 30, limit = 20)
        assertEquals(20, out.size)
        assertEquals(30, out.first().versionCode)
        assertEquals(11, out.last().versionCode)
    }

    @Test
    fun emptyWhenNothingIsOldEnough() {
        assertEquals(emptyList(), pastReleases(listOf(rel(5)), installedVersionCode = 4))
    }

    @Test
    fun splitsNotesIntoPointsJoiningWrappedLines() {
        val notes = "• У версий теперь настоящие номера.\n  Рядом — номер сборки.\n\n• Экран обновления.\n— Третий пункт"
        assertEquals(
            listOf("У версий теперь настоящие номера. Рядом — номер сборки.", "Экран обновления.", "Третий пункт"),
            ReleaseNote(1, "0.1", null, notes).items,
        )
    }

    @Test
    fun textWithoutMarkersIsOnePoint() {
        assertEquals(listOf("Исправления"), noteItems("Исправления"))
        assertEquals(emptyList(), noteItems("  \n "))
    }
}

package app.coshell.update

import app.coshell.update.data.parseReleaseHistory
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReleaseHistoryTest {
    @Test
    fun `reads entries with dates and trimmed notes`() {
        val text = """
            [{"versionCode": 41, "versionName": "0.37", "date": "2026-11-12", "notes": " • Новое \n", "extra": 1},
             {"versionCode": 40, "versionName": "0.36", "date": "вчера"},
             {"versionCode": 39, "versionName": "0.35"}]
        """.trimIndent()
        val out = parseReleaseHistory(text)
        assertEquals(listOf(41, 40, 39), out.map { it.versionCode })
        assertEquals(LocalDate(2026, 11, 12), out[0].date)
        assertEquals("• Новое", out[0].notes)
        assertNull(out[1].date)
        assertEquals("", out[2].notes)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `not a list is bad data`() {
        parseReleaseHistory("""{"versionCode": 1}""")
    }
}

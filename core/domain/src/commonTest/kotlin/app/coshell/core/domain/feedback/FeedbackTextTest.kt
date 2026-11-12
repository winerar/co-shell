package app.coshell.core.domain.feedback

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class FeedbackTextTest {
    private val device = DeviceInfo("0.1.0", 42, "Xiaomi 2211133C", "Android 15 (API 35)", "install-1")
    private fun report(text: String, lines: List<String>) = FeedbackReport(text, Instant.parse("2026-11-12T10:00:00Z"), device, lines)

    @Test
    fun `no technical lines keep the text as is`() {
        assertEquals("Не ставится", report("Не ставится", emptyList()).storedText())
    }

    @Test
    fun `technical lines follow the marker`() {
        val stored = report("Не ставится", listOf("installer: miui", "status 3")).storedText()
        assertEquals("Не ставится$DIAGNOSTICS_MARKER" + "installer: miui\nstatus 3", stored)
    }

    @Test
    fun `long technical data is cut in the middle to fit the rules limit`() {
        val lines = listOf("header") + List(300) { "2026-11-12T10:00:00Z event $it" } + listOf("last line")
        val stored = report("a".repeat(500), lines).storedText()
        assertEquals(STORED_TEXT_LIMIT, stored.length)
        assertTrue(stored.substringAfter(DIAGNOSTICS_MARKER).startsWith("header"))
        assertTrue(stored.endsWith("last line"))
        assertTrue("…" in stored)
    }
}

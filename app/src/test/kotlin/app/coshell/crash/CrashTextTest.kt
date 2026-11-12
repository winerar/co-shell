package app.coshell.crash

import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashTextTest {
    private val at = Instant.parse("2026-11-12T10:00:00.123Z")

    @Test
    fun `text has the header, the exception, its frames and the cause`() {
        val error = IllegalStateException("Boom: a.b", IllegalArgumentException("root"))
        val text = crashText(error, at, 42, "main")
        val lines = text.lines()
        assertEquals("crash at 2026-11-12T10:00:00Z, build 42, thread main", lines[0])
        assertEquals("java.lang.IllegalStateException: Boom: a.b", lines[1])
        assertTrue(lines[2].startsWith("  at "))
        assertTrue(lines.any { it == "caused by: java.lang.IllegalArgumentException: root" })
        assertTrue(lines.count { it.startsWith("  at ") } <= 30)
    }

    @Test
    fun `title is the short exception name and message`() {
        val text = crashText(IllegalStateException("Boom: a.b"), at, 42, "main")
        assertEquals("IllegalStateException: Boom: a.b", crashTitle(text))
        assertEquals("NullPointerException", crashTitle(crashText(NullPointerException(), at, 1, "main")))
    }

    @Test
    fun `recorder saves the crash and passes it on`() {
        val log = MemoryCrashLog()
        val clock = object : Clock { override fun now() = at }
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        var passedOn: Throwable? = null
        Thread.setDefaultUncaughtExceptionHandler { _, e -> passedOn = e }
        try {
            CrashRecorder(log, clock, 42).install()
            val error = RuntimeException("x")
            Thread.getDefaultUncaughtExceptionHandler()!!.uncaughtException(Thread.currentThread(), error)
            assertTrue(log.pending()!!.contains("java.lang.RuntimeException: x"))
            assertEquals(error, passedOn)
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previous)
        }
        log.clear()
        assertNull(log.pending())
    }
}

class MemoryCrashLog(private var text: String? = null) : CrashLog {
    override fun pending() = text
    override fun save(text: String) { this.text = text }
    override fun clear() { text = null }
}

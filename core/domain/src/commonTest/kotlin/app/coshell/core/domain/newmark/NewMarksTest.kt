package app.coshell.core.domain.newmark

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest

class NewMarksTest {
    private val store = FakeNewMarkStore()
    private val clock = MutableClock(Instant.parse("2026-11-15T10:00:00Z"))

    private fun marks(wasUpdated: Boolean = true) = NewMarks(store, clock, wasUpdated)

    @Test
    fun `an update shows the keys it brings`() = runTest {
        assertEquals(setOf("recurring"), marks().current(listOf("recurring")))
    }

    @Test
    fun `a fresh install marks nothing, not even later`() = runTest {
        assertEquals(emptySet(), marks(wasUpdated = false).current(listOf("recurring")))
        clock.now += 1.hours
        assertEquals(emptySet(), marks(wasUpdated = false).current(listOf("recurring")))
    }

    @Test
    fun `after a fresh install a key from the next version is new`() = runTest {
        marks(wasUpdated = false).current(listOf("recurring"))
        assertEquals(setOf("reports"), marks(wasUpdated = true).current(listOf("recurring", "reports")))
    }

    @Test
    fun `the mark lasts seven days from the first time it was shown`() = runTest {
        marks().current(listOf("recurring"))
        clock.now += 7.days - 1.hours
        assertEquals(setOf("recurring"), marks().current(listOf("recurring")))
        clock.now += 1.hours
        assertEquals(emptySet(), marks().current(listOf("recurring")))
    }

    @Test
    fun `the clock starts when the key is first seen, not at the update`() = runTest {
        marks().current(listOf("recurring"))
        clock.now += 6.days
        assertEquals(setOf("recurring", "reports"), marks().current(listOf("recurring", "reports")))
        clock.now += 2.days
        assertEquals(setOf("reports"), marks().current(listOf("recurring", "reports")))
    }

    @Test
    fun `opening the place removes its mark only`() = runTest {
        val marks = marks()
        marks.current(listOf("recurring", "reports"))
        marks.opened("recurring")
        assertEquals(setOf("reports"), marks.current(listOf("recurring", "reports")))
    }

    @Test
    fun `a place split out of an opened one is not new`() = runTest {
        marks().current(listOf("settings"))
        marks().opened("settings")
        assertEquals(emptySet(), marks().current(listOf("settings", "settings.bar"), mapOf("settings.bar" to "settings")))
    }

    @Test
    fun `a place split out of a marked one keeps its days`() = runTest {
        marks().current(listOf("settings"))
        clock.now += 5.days
        val inherits = mapOf("settings.bar" to "settings")
        assertEquals(setOf("settings", "settings.bar"), marks().current(listOf("settings", "settings.bar"), inherits))
        clock.now += 2.days
        assertEquals(emptySet(), marks().current(listOf("settings", "settings.bar"), inherits))
    }

    @Test
    fun `a split place has its own mark once seen`() = runTest {
        val inherits = mapOf("settings.bar" to "settings", "settings.density" to "settings")
        marks().current(listOf("settings", "settings.bar", "settings.density"), inherits)
        marks().opened("settings.bar")
        assertEquals(setOf("settings", "settings.density"), marks().current(listOf("settings", "settings.bar", "settings.density"), inherits))
    }
}

private class MutableClock(var now: Instant) : Clock {
    override fun now(): Instant = now
}

private class FakeNewMarkStore : NewMarkStore {
    private var started = false
    private val shown = mutableMapOf<String, Instant>()
    private val opened = mutableSetOf<String>()

    override suspend fun isStarted() = started
    override suspend fun setStarted() { started = true }
    override suspend fun shownSince(key: String) = shown[key]
    override suspend fun setShownSince(key: String, at: Instant) { shown[key] = at }
    override suspend fun isOpened(key: String) = key in opened
    override suspend fun setOpened(key: String) { opened += key }
}

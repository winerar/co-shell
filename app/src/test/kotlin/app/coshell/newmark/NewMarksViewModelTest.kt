package app.coshell.newmark

import app.coshell.core.domain.newmark.NewMarkStore
import app.coshell.core.domain.newmark.NewMarks
import app.coshell.testing.MainDispatcherRule
import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NewMarksViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val store = MemoryStore()
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-15T10:00:00Z")
    }

    private fun viewModel(wasUpdated: Boolean = true) = NewMarksViewModel(NewMarks(store, clock, wasUpdated))

    @Test
    fun `after an update the recurring row is new`() {
        assertEquals(setOf(NewFeatures.RECURRING), viewModel().current.value)
    }

    @Test
    fun `after a fresh install nothing is new`() {
        assertEquals(emptySet<String>(), viewModel(wasUpdated = false).current.value)
    }

    @Test
    fun `opening the row removes the mark now and on the next start`() {
        viewModel().opened(NewFeatures.RECURRING)
        assertEquals(emptySet<String>(), viewModel().current.value)
    }

    @Test
    fun `the mark goes away on screen at once`() {
        val vm = viewModel()
        vm.opened(NewFeatures.RECURRING)
        assertEquals(emptySet<String>(), vm.current.value)
    }
}

private class MemoryStore : NewMarkStore {
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

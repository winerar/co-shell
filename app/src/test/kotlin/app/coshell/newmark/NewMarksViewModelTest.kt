package app.coshell.newmark

import app.coshell.core.domain.newmark.NewMarkStore
import app.coshell.core.domain.newmark.NewMarkTree
import app.coshell.core.domain.newmark.NewMarks
import app.coshell.testing.MainDispatcherRule
import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NewMarksViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val store = MemoryStore()
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-15T10:00:00Z")
    }

    private fun viewModel(wasUpdated: Boolean = true) = NewMarksViewModel(NewMarkCenter(NewMarks(store, clock, wasUpdated)))

    @Test
    fun `after an update every new place is marked`() {
        assertEquals(EVERYWHERE, viewModel().current.value)
    }

    @Test
    fun `after a fresh install nothing is new`() {
        assertEquals(emptySet<String>(), viewModel(wasUpdated = false).current.value)
    }

    @Test
    fun `opening the row removes the mark now and on the next start`() {
        viewModel().opened(NewFeatures.RECURRING)
        assertEquals(EVERYWHERE - NewFeatures.RECURRING, viewModel().current.value)
    }

    @Test
    fun `the mark goes away on screen at once`() {
        val vm = viewModel()
        vm.opened(NewFeatures.RECURRING)
        assertEquals(EVERYWHERE - NewFeatures.RECURRING, vm.current.value)
    }

    @Test
    fun `opening a deep place keeps the way marked while something else inside is new`() {
        viewModel().opened(NewFeatures.SETTINGS_BAR)
        val marks = viewModel().current.value
        assertTrue(NewFeatures.SETTINGS in marks && NewFeatures.SETTINGS_LOOK in marks)
        assertFalse(NewFeatures.SETTINGS_SCREENS in marks || NewFeatures.SETTINGS_BAR in marks)
    }

    @Test
    fun `the way loses its mark when everything inside is opened`() {
        val vm = viewModel()
        vm.opened(NewFeatures.SETTINGS_BAR)
        vm.opened(NewFeatures.SETTINGS_DENSITY)
        assertFalse(NewFeatures.SETTINGS in vm.current.value)
    }

    @Test
    fun `tapping a way takes nothing away`() {
        val vm = viewModel()
        vm.opened(NewFeatures.SETTINGS)
        assertEquals(EVERYWHERE, vm.current.value)
    }

    @Test
    fun `screens share one state`() {
        val center = NewMarkCenter(NewMarks(store, clock, true))
        val menu = NewMarksViewModel(center)
        NewMarksViewModel(center).opened(NewFeatures.SETTINGS_BAR)
        assertFalse(NewFeatures.SETTINGS_BAR in menu.current.value)
    }

    @Test
    fun `a phone that already opened Settings sees nothing new inside`() {
        val old = NewMarks(store, clock, true)
        kotlinx.coroutines.runBlocking {
            old.current(listOf(NewFeatures.SETTINGS))
            old.opened(NewFeatures.SETTINGS)
        }
        assertEquals(emptySet<String>(), viewModel().current.value.intersect(NewFeatures.inside.keys + NewFeatures.deeper))
    }
}

/** Every place marked after an update: the places themselves and the ways to them. */
internal val EVERYWHERE = NewMarkTree.marked(NewFeatures.all.toSet(), NewFeatures.inside)

internal class MemoryStore : NewMarkStore {
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

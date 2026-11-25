package app.coshell.release

import app.coshell.core.domain.newmark.NewMarks
import app.coshell.newmark.MemoryStore
import app.coshell.newmark.NewFeatures
import app.coshell.newmark.NewMarksViewModel
import app.coshell.testing.MainDispatcherRule
import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReleaseNoticeViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val store = MemoryStore()
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-25T10:00:00Z")
    }

    private fun marks(wasUpdated: Boolean = true) = NewMarks(store, clock, wasUpdated)

    @Test
    fun `after an update the card is on Overview`() {
        assertTrue(ReleaseNoticeViewModel(marks()).visible.value)
    }

    @Test
    fun `a fresh install has no card`() {
        assertFalse(ReleaseNoticeViewModel(marks(wasUpdated = false)).visible.value)
    }

    @Test
    fun `a fresh install that opened More first still has no card`() {
        NewMarksViewModel(marks(wasUpdated = false))
        assertFalse(ReleaseNoticeViewModel(marks(wasUpdated = false)).visible.value)
    }

    @Test
    fun `open or close puts the card away for good`() {
        val vm = ReleaseNoticeViewModel(marks())
        vm.done()
        assertFalse(vm.visible.value)
        assertFalse(ReleaseNoticeViewModel(marks()).visible.value)
    }

    @Test
    fun `the release key does not mark a row of More`() {
        ReleaseNoticeViewModel(marks())
        assertEquals(NewFeatures.all.toSet(), NewMarksViewModel(marks()).current.value)
    }
}

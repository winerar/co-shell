package app.coshell.hints

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Знакомство с приложением» on «Обзор» (#598): new installs only, gone once opened or closed, back after a reset. */
class TourCardTest {
    @Test
    fun `a new install gets the card`() = runTest {
        val store = FakeHintStore().apply { checked = false }
        val center = HintCenter(store) { 100L }
        center.checkTour { false }
        assertTrue(center.tour.value)
        assertTrue(store.checked)
    }

    @Test
    fun `a phone already in use never gets it, even on the next launch`() = runTest {
        val store = FakeHintStore().apply { checked = false }
        HintCenter(store) { 100L }.checkTour { true }
        val next = HintCenter(store) { 100L }
        next.checkTour { true }
        assertFalse(next.tour.value)
    }

    @Test
    fun `opened or closed it does not come back`() = runTest {
        val store = FakeHintStore().apply { checked = false }
        val center = HintCenter(store) { 100L }
        center.checkTour { false }
        center.tourDone()
        assertFalse(center.tour.value)
        val next = HintCenter(store) { 100L }
        next.checkTour { false }
        assertFalse(next.tour.value)
    }

    @Test
    fun `show hints again brings the card back, also on a phone in use`() = runTest {
        val store = FakeHintStore().apply { checked = false }
        val center = HintCenter(store) { 100L }
        center.checkTour { true }
        assertFalse(center.tour.value)
        center.reset()
        assertTrue(center.tour.value)
        val next = HintCenter(store) { 100L }
        next.checkTour { true }
        assertEquals(true, next.tour.value)
    }

    @Test
    fun `hints off hide the card and on bring it back if not seen`() = runTest {
        val store = FakeHintStore().apply { checked = false }
        val center = HintCenter(store) { 100L }
        center.checkTour { false }
        center.setEnabled(false)
        assertFalse(center.tour.value)
        center.setEnabled(true)
        assertTrue(center.tour.value)
    }

    @Test
    fun `six stories in the order of creating things`() {
        assertEquals(6, TourStories.all.size)
        assertEquals(TourStories.all.size, TourStories.all.map { it.title }.toSet().size)
    }
}

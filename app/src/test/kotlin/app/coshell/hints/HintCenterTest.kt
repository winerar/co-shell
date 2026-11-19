package app.coshell.hints

import app.coshell.core.domain.hints.HintId
import app.coshell.core.domain.hints.HintStore
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlinx.coroutines.test.runTest

private class FakeHintStore(var on: Boolean = true) : HintStore {
    val seen = mutableSetOf<String>()
    var day: Long? = null
    override suspend fun enabled() = on
    override suspend fun setEnabled(value: Boolean) { on = value }
    override suspend fun shown(): Set<String> = seen.toSet()
    override suspend fun markShown(key: String) { seen += key }
    override suspend fun lastShownDay() = day
    override suspend fun setLastShownDay(day: Long) { this.day = day }
    override suspend fun stepsHidden() = false
    override suspend fun setStepsHidden(value: Boolean) = Unit
    override suspend fun firstSeenDay(today: Long) = today
    override suspend fun firstEntryDay(candidate: Long?) = candidate
    override suspend fun reset() { seen.clear(); day = null; on = true }
}

class HintCenterTest {
    private var today = 100L

    @Test
    fun `shows a hint once and remembers it`() = runTest {
        val store = FakeHintStore()
        assertTrue(HintCenter(store) { today }.claim(HintId.ACCOUNTS))
        assertEquals(setOf("accounts"), store.seen)
        // A fresh visit on another day: already shown, so never again.
        today = 101
        assertFalse(HintCenter(store) { today }.claim(HintId.ACCOUNTS))
    }

    @Test
    fun `gives one hint per visit and one per day`() = runTest {
        val store = FakeHintStore()
        val center = HintCenter(store) { today }
        assertTrue(center.claim(HintId.ACCOUNTS))
        assertFalse(center.claim(HintId.CATEGORIES))
        // New visit, same day: still only one a day.
        assertFalse(HintCenter(store) { today }.claim(HintId.CATEGORIES))
        today = 101
        assertTrue(HintCenter(store) { today }.claim(HintId.CATEGORIES))
    }

    @Test
    fun `shows nothing while hints are off and again after a reset`() = runTest {
        val store = FakeHintStore(on = false)
        assertFalse(HintCenter(store) { today }.claim(HintId.BUDGETS))
        store.reset()
        assertTrue(HintCenter(store) { today }.claim(HintId.BUDGETS))
    }

    @Test
    fun `a warm moment comes after the daily hint and does not use up the day`() = runTest {
        val store = FakeHintStore()
        assertTrue(HintCenter(store) { today }.claim(HintId.ACCOUNTS))
        val center = HintCenter(store) { today }
        assertTrue(center.claim(HintId.FIRST_ENTRY))
        assertFalse(center.claim(HintId.FIRST_ENTRY))
        // A moment alone leaves the day free for a word hint.
        val other = FakeHintStore()
        val c2 = HintCenter(other) { today }
        assertTrue(c2.claim(HintId.FIRST_LIMIT))
        assertTrue(c2.claim(HintId.CATEGORIES))
    }
}

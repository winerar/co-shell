package app.coshell.hints

import app.coshell.R
import app.coshell.core.domain.hints.HintId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The banner queue and the one app-wide hints state (onboarding check 2026-11-25). */
class HintCenterStateTest {
    @Test
    fun `a second moment waits for the first instead of replacing it`() = runTest {
        val center = HintCenter(FakeHintStore()) { 100L }
        assertTrue(center.offer(mapOf(HintId.THEME to R.string.hint_theme)))
        assertTrue(center.offer(mapOf(HintId.LIMIT_80 to R.string.hint_limit_80), mapOf(HintId.LIMIT_80 to listOf("Food", "90 USD"))))
        assertEquals(HintId.THEME, center.banner.value?.id)
        center.dismiss()
        assertEquals(HintId.LIMIT_80, center.banner.value?.id)
        assertEquals(listOf("Food", "90 USD"), center.banner.value?.args)
        center.dismiss()
        assertNull(center.banner.value)
    }

    @Test
    fun `the reset notice queues behind a card on screen`() = runTest {
        val center = HintCenter(FakeHintStore()) { 100L }
        center.offer(mapOf(HintId.FIRST_ENTRY to R.string.hint_first_entry))
        center.say(R.string.hints_reset_done)
        center.dismiss()
        assertEquals(R.string.hints_reset_done, center.banner.value?.text)
    }

    @Test
    fun `state is read once and shared, unknown until then`() = runTest {
        val store = FakeHintStore(on = false).apply { hidden = true }
        val center = HintCenter(store) { 100L }
        assertNull(center.enabled.value)
        assertNull(center.stepsHidden.value)
        center.load()
        assertEquals(false, center.enabled.value)
        assertEquals(true, center.stepsHidden.value)
    }

    @Test
    fun `turning hints off clears the banner and the queue`() = runTest {
        val store = FakeHintStore()
        val center = HintCenter(store) { 100L }
        center.offer(mapOf(HintId.THEME to R.string.hint_theme))
        center.offer(mapOf(HintId.BACKUP to R.string.hint_backup))
        center.setEnabled(false)
        assertNull(center.banner.value)
        center.dismiss()
        assertNull(center.banner.value)
        assertFalse(store.on)
    }

    @Test
    fun `reset brings the steps back, turns hints on and says so`() = runTest {
        val store = FakeHintStore(on = false).apply { hidden = true }
        val center = HintCenter(store) { 100L }
        center.load()
        center.reset()
        assertEquals(true, center.enabled.value)
        assertEquals(false, center.stepsHidden.value)
        assertFalse(store.hidden)
        assertEquals(R.string.hints_reset_done, center.banner.value?.text)
    }

    @Test
    fun `launch moments are taken once per process`() {
        val center = HintCenter(FakeHintStore()) { 100L }
        assertTrue(center.takeLaunchMoments())
        assertFalse(center.takeLaunchMoments())
    }
}

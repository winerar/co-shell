package app.coshell.privacy

import app.coshell.core.designsystem.components.keypad.PinKey
import app.coshell.core.domain.privacy.PinAttempts
import app.coshell.core.domain.privacy.PinHasher
import app.coshell.core.domain.privacy.PrivacyOptions
import app.coshell.core.domain.privacy.PrivacySettings
import app.coshell.core.domain.privacy.StoredPin
import app.coshell.privacy.pin.PinFlow
import app.coshell.privacy.pin.PinGoal
import app.coshell.privacy.pin.PinNote
import app.coshell.privacy.pin.PinStep
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PinFlowTest {
    private class FakeSettings : PrivacySettings {
        val state = MutableStateFlow(PrivacyOptions())
        var stored: StoredPin? = null
        var tries = PinAttempts()
        override val options = state
        override suspend fun update(change: (PrivacyOptions) -> PrivacyOptions) { state.value = change(state.value) }
        override suspend fun pin() = stored
        override suspend fun setPin(pin: StoredPin?) {
            stored = pin
            state.value = state.value.copy(pinEnabled = pin != null)
        }
        override suspend fun attempts() = tries
        override suspend fun setAttempts(attempts: PinAttempts) { tries = attempts }
    }

    private val hasher = object : PinHasher {
        override fun newSalt() = "salt"
        override fun hash(pin: String, salt: String) = "$salt:$pin"
    }

    private fun TestScope.center(settings: FakeSettings, now: () -> Long = { testScheduler.currentTime }, elapsed: () -> Long = now) =
        PrivacyCenter(settings, hasher, now, elapsed, kotlin.coroutines.EmptyCoroutineContext, backgroundScope)

    private fun PinFlow.type(digits: String) = digits.forEach { key(PinKey.Digit(it)) }

    @Test
    fun `creating a pin asks twice and turns the lock on`() = runTest(StandardTestDispatcher()) {
        val settings = FakeSettings()
        val center = center(settings)
        runCurrent()
        val flow = PinFlow(center, backgroundScope, PinStep.CREATE, PinGoal.SET)
        flow.type("1234")
        assertEquals(PinStep.REPEAT, flow.state.value.step)
        flow.type("1235")
        assertEquals(PinStep.CREATE, flow.state.value.step)
        assertEquals(PinNote.MISMATCH, flow.state.value.note)
        flow.type("4321")
        flow.type("4321")
        runCurrent()
        assertTrue(flow.state.value.done)
        assertEquals(StoredPin("salt", "salt:4321"), settings.stored)
        assertTrue(settings.state.value.pinEnabled)
        assertFalse(center.locked.value)
    }

    @Test
    fun `a forgotten pin is replaced after the phone confirms it is the owner`() = runTest(StandardTestDispatcher()) {
        val settings = FakeSettings().apply { stored = StoredPin("salt", "salt:1111"); tries = PinAttempts(failed = 3) }
        settings.state.value = PrivacyOptions(pinEnabled = true)
        val center = center(settings)
        runCurrent()
        assertTrue(center.locked.value)
        val flow = PinFlow(center, backgroundScope, PinStep.ENTER, PinGoal.UNLOCK)
        // «Забыли PIN?» → the phone's own lock confirmed → the lock screen starts a new PIN (LockViewModel.deviceConfirmed).
        flow.restart(PinStep.CREATE, PinGoal.SET)
        flow.type("2468")
        assertEquals(PinStep.REPEAT, flow.state.value.step)
        flow.type("2468")
        runCurrent()
        assertTrue(flow.state.value.done)
        assertEquals(StoredPin("salt", "salt:2468"), settings.stored)
        assertTrue(settings.state.value.pinEnabled)
        assertFalse(center.locked.value)

        // The old PIN no longer opens anything: the next start is locked again, check both.
        val next = center(settings)
        runCurrent()
        assertTrue(next.locked.value)
        val again = PinFlow(next, backgroundScope, PinStep.ENTER, PinGoal.UNLOCK)
        again.type("1111")
        runCurrent()
        assertEquals(PinNote.WRONG, again.state.value.note)
        assertTrue(next.locked.value)
        again.type("2468")
        runCurrent()
        assertFalse(next.locked.value)
    }

    @Test
    fun `the lock opens with the right pin and waits after five wrong ones`() = runTest(StandardTestDispatcher()) {
        val settings = FakeSettings().apply { stored = StoredPin("salt", "salt:1111") }
        settings.state.value = PrivacyOptions(pinEnabled = true)
        val center = center(settings)
        runCurrent()
        assertTrue(center.locked.value)
        val flow = PinFlow(center, backgroundScope, PinStep.ENTER, PinGoal.UNLOCK)
        repeat(4) {
            flow.type("0000")
            runCurrent()
            assertEquals(PinNote.WRONG, flow.state.value.note)
        }
        flow.type("0000")
        runCurrent()
        assertEquals(PinNote.WAIT, flow.state.value.note)
        assertEquals(30, flow.state.value.waitSeconds)
        assertFalse(flow.state.value.enabled)
        flow.type("1111") // keys are off while waiting
        assertEquals("", flow.state.value.entry.digits)
        advanceTimeBy(30_001)
        runCurrent()
        assertTrue(flow.state.value.enabled)
        flow.type("1111")
        runCurrent()
        assertTrue(flow.state.value.done)
        assertFalse(center.locked.value)
        assertEquals(0, settings.tries.failed)
    }

    @Test
    fun `turning the pin off needs the current one`() = runTest(StandardTestDispatcher()) {
        val settings = FakeSettings().apply { stored = StoredPin("salt", "salt:2468") }
        settings.state.value = PrivacyOptions(pinEnabled = true, biometric = true)
        val center = center(settings)
        runCurrent()
        val flow = PinFlow(center, backgroundScope, PinStep.CURRENT, PinGoal.DISABLE)
        flow.type("2468")
        runCurrent()
        assertTrue(flow.state.value.done)
        assertNull(settings.stored)
        assertFalse(settings.state.value.pinEnabled)
    }

    @Test
    fun `relocks after the delay and hides on a new visit`() = runTest(StandardTestDispatcher()) {
        var clock = 0L
        val settings = FakeSettings().apply { stored = StoredPin("salt", "salt:1111") }
        settings.state.value = PrivacyOptions(pinEnabled = true, hideOnLaunch = true)
        val center = center(settings, now = { clock }, elapsed = { clock })
        runCurrent()
        assertTrue(center.hidden.value)
        center.toggleHidden()
        center.unlock()
        center.onBackground()
        clock = 30_000
        center.onForeground()
        assertFalse(center.locked.value)
        assertFalse(center.hidden.value)
        center.onBackground()
        clock = 100_000
        center.onForeground()
        assertTrue(center.locked.value)
        assertTrue(center.hidden.value)
    }

    @Test
    fun `face down hides the sums and shows the pill`() = runTest(StandardTestDispatcher()) {
        val settings = FakeSettings()
        val center = center(settings)
        runCurrent()
        assertFalse(center.locked.value)
        center.onFaceDown()
        assertTrue(center.hidden.value)
        assertTrue(center.faceDownNotice.value)
        center.toggleHidden()
        assertFalse(center.hidden.value)
        assertFalse(center.faceDownNotice.value)
        settings.state.value = PrivacyOptions(hideFaceDown = false)
        runCurrent()
        center.onFaceDown()
        assertFalse(center.hidden.value)
    }
}

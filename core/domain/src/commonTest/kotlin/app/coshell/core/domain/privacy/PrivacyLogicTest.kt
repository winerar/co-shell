package app.coshell.core.domain.privacy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrivacyLogicTest {
    private val pin = PrivacyOptions(pinEnabled = true)

    @Test
    fun `starts locked only with a pin`() {
        val lock = AppLock()
        lock.onOptions(PrivacyOptions())
        assertFalse(lock.locked)
        assertTrue(AppLock().also { it.onOptions(pin) }.locked)
    }

    @Test
    fun `relocks after the chosen delay`() {
        val lock = AppLock().apply { onOptions(pin); unlock() }
        lock.onBackground(1_000)
        lock.onForeground(1_000 + 59_999, pin)
        assertFalse(lock.locked)
        lock.onBackground(100_000)
        lock.onForeground(160_000, pin)
        assertTrue(lock.locked)
    }

    @Test
    fun `relock now locks on any return`() {
        val now = pin.copy(relock = RelockDelay.NOW)
        val lock = AppLock().apply { onOptions(now); unlock() }
        lock.onBackground(5)
        lock.onForeground(5, now)
        assertTrue(lock.locked)
    }

    @Test
    fun `the first background moment counts`() {
        val lock = AppLock().apply { onOptions(pin); unlock() }
        lock.onBackground(0)
        lock.onBackground(50_000)
        lock.onForeground(61_000, pin)
        assertTrue(lock.locked)
    }

    @Test
    fun `turning the pin off unlocks`() {
        val lock = AppLock().apply { onOptions(pin) }
        lock.onOptions(PrivacyOptions())
        assertFalse(lock.locked)
    }

    @Test
    fun `five wrong attempts make the person wait, then longer`() {
        var a = PinAttempts()
        repeat(4) { a = a.fail(0) }
        assertEquals(0, a.waitLeft(0))
        a = a.fail(1_000)
        assertEquals(30_000, a.waitLeft(1_000))
        a = a.fail(40_000)
        assertEquals(60_000, a.waitLeft(40_000))
        repeat(10) { a = a.fail(100_000) }
        assertEquals(PinAttempts.MAX_WAIT, a.waitLeft(100_000))
        assertEquals(0, a.waitLeft(100_000 + PinAttempts.MAX_WAIT))
    }

    @Test
    fun `pin entry takes four digits`() {
        var e = PinEntry()
        "12a345".forEach { e = e.type(it) }
        assertEquals("1234", e.digits)
        assertTrue(e.complete)
        assertEquals("123", e.erase().digits)
        assertEquals("", PinEntry().erase().digits)
    }

    @Test
    fun `stored pin matches only the same digits`() {
        val hasher = object : PinHasher {
            override fun newSalt() = "s"
            override fun hash(pin: String, salt: String) = salt + pin.reversed()
        }
        val stored = hasher.store("1234")
        assertTrue(stored.matches("1234", hasher))
        assertFalse(stored.matches("1243", hasher))
    }

    @Test
    fun `relock parse falls back to one minute`() {
        assertEquals(RelockDelay.FIVE_MINUTES, RelockDelay.parse("FIVE_MINUTES"))
        assertEquals(RelockDelay.ONE_MINUTE, RelockDelay.parse("x"))
        assertTrue(PrivacyOptions(blockScreenshots = true).previewHidden)
    }
}

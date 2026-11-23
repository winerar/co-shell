package app.coshell.core.domain.access

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

class AccessGateTest {
    private class FakeRemote : AccessRemote {
        val codes = mutableMapOf<String, CodeLookup>()
        var offline = false
        var added = mutableListOf<Pair<String, String>>()
        var refuseAdd = false
        override suspend fun lookup(code: String): CodeLookup {
            if (offline) error("offline")
            return codes[code] ?: CodeLookup.NotFound
        }
        override suspend fun addDevice(code: String, installationId: String): Boolean {
            if (offline) error("offline")
            if (refuseAdd) return false
            added += code to installationId
            return true
        }
    }

    private class FakeStore : AccessStore {
        var stored: StoredAccess? = null
        override suspend fun load() = stored
        override suspend fun save(access: StoredAccess) { stored = access }
        override suspend fun installationId() = "phone-1"
    }

    private val remote = FakeRemote()
    private val store = FakeStore()
    private var now = 1_000_000L
    private val gate = AccessGate(remote, store) { now }
    private val code = "K7QXM2PD9WTA"

    @Test
    fun `a typed code is normalised the way the robot makes it`() {
        assertEquals(code, AccessCode.normalize("k7qx-m2pd-9wta"))
        assertEquals(code, AccessCode.normalize(" K7QX M2PD 9WTA "))
        assertNull(AccessCode.normalize("K7QX-M2PD"))
        assertNull(AccessCode.normalize("K7QX-M2PD-9WT0"), "0 is not in the alphabet")
        assertNull(AccessCode.normalize("K7QX-M2PD-9WTI"), "I is not in the alphabet")
        assertEquals("K7QX-M2PD-9WTA", AccessCode.display(code))
    }

    @Test
    fun `the field groups what is typed by four and stops at twelve`() {
        assertEquals("K7Q", AccessCode.format("k7q"))
        assertEquals("K7QX", AccessCode.format("k7qx"))
        assertEquals("K7QX-M2", AccessCode.format("k7qxm2"))
        assertEquals("K7QX-M2PD-9WTA", AccessCode.format("k7qx-m2pd-9wta-extra"))
    }

    @Test
    fun `a good code is checked online, the phone is added and the code is kept`() = runTest {
        remote.codes[code] = CodeLookup.Valid("Аня", emptyList(), 3)
        assertEquals(EnterResult.Granted("Аня"), gate.enter("k7qx-m2pd-9wta"))
        assertEquals(listOf(code to "phone-1"), remote.added)
        assertEquals(StoredAccess(code, "Аня", now), store.stored)
    }

    @Test
    fun `an unknown, malformed or revoked code is refused and nothing is kept`() = runTest {
        assertEquals(EnterResult.Malformed, gate.enter("abc"))
        assertEquals(EnterResult.NotFound, gate.enter(code))
        remote.codes[code] = CodeLookup.Revoked("Аня")
        assertEquals(EnterResult.Revoked, gate.enter(code))
        assertNull(store.stored)
        assertTrue(remote.added.isEmpty())
    }

    @Test
    fun `a code on every allowed phone refuses a new one, but this phone may enter again`() = runTest {
        remote.codes[code] = CodeLookup.Valid("Аня", listOf("a", "b", "c"), 3)
        assertEquals(EnterResult.Full, gate.enter(code))
        remote.codes[code] = CodeLookup.Valid("Аня", listOf("a", "phone-1", "c"), 3)
        assertEquals(EnterResult.Granted("Аня"), gate.enter(code))
        assertTrue(remote.added.isEmpty(), "already registered: nothing to add")
    }

    @Test
    fun `the server may refuse the device even when the list looked free`() = runTest {
        remote.codes[code] = CodeLookup.Valid("Аня", emptyList(), 3)
        remote.refuseAdd = true
        assertEquals(EnterResult.Full, gate.enter(code))
        assertNull(store.stored)
    }

    @Test
    fun `entering needs the network`() = runTest {
        remote.offline = true
        assertFailsWith<IllegalStateException> { gate.enter(code) }
    }

    @Test
    fun `the code is asked about again after three days`() = runTest {
        remote.codes[code] = CodeLookup.Valid("Аня", emptyList(), 3)
        gate.enter(code)
        assertFalse(gate.recheckDue(store.stored))
        now += 2.days.inWholeMilliseconds
        assertFalse(gate.recheckDue(store.stored))
        now += 1.days.inWholeMilliseconds
        assertTrue(gate.recheckDue(store.stored))
        assertFalse(gate.recheckDue(null))
    }

    @Test
    fun `a revoked code closes the phone on the next check, offline it does not`() = runTest {
        remote.codes[code] = CodeLookup.Valid("Аня", emptyList(), 3)
        gate.enter(code)
        now += 5.days.inWholeMilliseconds

        remote.offline = true
        assertEquals(false, gate.recheck()?.revoked, "no network: the app stays open")

        remote.offline = false
        assertEquals(now, gate.recheck()?.checkedAtMs)
        remote.codes[code] = CodeLookup.Revoked("Аня")
        val closed = gate.recheck()
        assertEquals(true, closed?.revoked)
        assertEquals(closed, store.stored)
        assertFalse(gate.recheckDue(closed), "nothing to ask once it is closed")
    }

    @Test
    fun `a code that vanished from the server counts as revoked`() = runTest {
        remote.codes[code] = CodeLookup.Valid("Аня", emptyList(), 3)
        gate.enter(code)
        remote.codes.clear()
        assertEquals(true, gate.recheck()?.revoked)
    }

    @Test
    fun `entering a new code after being closed opens the phone again`() = runTest {
        store.stored = StoredAccess(code, "Аня", 1L, revoked = true)
        val other = "ABCDEFGHJKMN"
        remote.codes[other] = CodeLookup.Valid("Паша", emptyList(), 3)
        assertIs<EnterResult.Granted>(gate.enter(other))
        assertEquals(false, store.stored?.revoked)
        assertEquals(other, store.stored?.code)
    }
}

package app.coshell.access

import app.coshell.core.domain.access.AccessGate
import app.coshell.core.domain.access.AccessRemote
import app.coshell.core.domain.access.AccessStore
import app.coshell.core.domain.access.CodeLookup
import app.coshell.core.domain.access.StoredAccess
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessCenterTest {
    private class Remote : AccessRemote {
        val codes = mutableMapOf<String, CodeLookup>()
        var offline = false
        override suspend fun lookup(code: String): CodeLookup {
            if (offline) error("offline")
            return codes[code] ?: CodeLookup.NotFound
        }
        override suspend fun addDevice(code: String, installationId: String) = true
    }

    private class Store(var stored: StoredAccess? = null) : AccessStore {
        override suspend fun load() = stored
        override suspend fun save(access: StoredAccess) { stored = access }
        override suspend fun installationId() = "phone-1"
    }

    private val code = "K7QXM2PD9WTA"
    private val remote = Remote()
    private val store = Store()
    private var now = 10_000_000_000L
    private fun center(on: Boolean = true) = AccessCenter(AccessGate(remote, store) { now }, store, AccessEnabled(on))

    @Test
    fun `a build without the gate is open and never asks anything`() = runTest {
        remote.offline = true
        val center = center(on = false)
        assertEquals(AccessState.Open, center.state.value)
        center.start()
        center.recheck()
        assertEquals(AccessState.Open, center.state.value)
    }

    @Test
    fun `nothing is known until the code is read, then the code screen or the app`() = runTest {
        val center = center()
        assertEquals(AccessState.Loading, center.state.value)
        center.start()
        assertEquals(AccessState.Required, center.state.value)

        store.stored = StoredAccess(code, "Аня", now)
        val other = center()
        other.start()
        assertEquals(AccessState.Granted("Аня"), other.state.value)
        assertFalse(other.note.value)
    }

    @Test
    fun `a closed code shows the window on opening`() = runTest {
        store.stored = StoredAccess(code, "Аня", now, revoked = true)
        val center = center()
        center.start()
        assertEquals(AccessState.Revoked("Аня"), center.state.value)
        assertTrue(center.note.value)
        center.dismissNote()
        assertFalse(center.note.value)
        center.showNote()
        assertTrue("a blocked tap shows it again", center.note.value)
    }

    @Test
    fun `a good code opens the app, a wrong one says why`() = runTest {
        val center = center()
        center.start()
        center.submit("abc")
        assertEquals(AccessError.MALFORMED, center.entry.value.error)
        center.submit("K7QX-M2PD-9WTA")
        assertEquals(AccessError.NOT_FOUND, center.entry.value.error)
        remote.codes[code] = CodeLookup.Revoked("Аня")
        center.submit("K7QX-M2PD-9WTA")
        assertEquals(AccessError.REVOKED, center.entry.value.error)
        remote.offline = true
        center.submit("K7QX-M2PD-9WTA")
        assertEquals(AccessError.NETWORK, center.entry.value.error)
        assertEquals(AccessState.Required, center.state.value)

        remote.offline = false
        remote.codes[code] = CodeLookup.Valid("Аня", emptyList(), 3)
        center.submit("K7QX-M2PD-9WTA")
        assertEquals(AccessState.Granted("Аня"), center.state.value)
        assertNull(center.entry.value.error)
        assertFalse(center.entry.value.busy)
    }

    @Test
    fun `after three days a revoked code closes the open app`() = runTest {
        store.stored = StoredAccess(code, "Аня", now)
        remote.codes[code] = CodeLookup.Revoked("Аня")
        val center = center()
        center.start()
        assertEquals("checked a moment ago: no question yet", AccessState.Granted("Аня"), center.state.value)

        now += 4.days.inWholeMilliseconds
        center.recheck()
        assertEquals(AccessState.Revoked("Аня"), center.state.value)
        assertTrue(center.note.value)
    }

    @Test
    fun `a closed phone takes another code from the window`() = runTest {
        store.stored = StoredAccess(code, "Аня", 1L, revoked = true)
        val center = center()
        center.start()
        center.changeCode()
        assertTrue(center.entering.value)
        assertFalse(center.note.value)
        center.cancelEntering()
        assertFalse(center.entering.value)

        center.changeCode()
        remote.codes["ABCDEFGHJKMN"] = CodeLookup.Valid("Паша", emptyList(), 3)
        center.submit("abcd-efgh-jkmn")
        assertEquals(AccessState.Granted("Паша"), center.state.value)
        assertFalse(center.entering.value)
    }
}

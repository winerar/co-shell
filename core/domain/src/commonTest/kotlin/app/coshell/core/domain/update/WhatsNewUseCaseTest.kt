package app.coshell.core.domain.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class WhatsNewUseCaseTest {
    private val store = FakeUpdateStateStore()
    private val whatsNew = WhatsNewUseCase(store, InstalledVersion(11, "0.1.11"))

    @Test
    fun `shows notes once after updating to the remembered release`() = runTest {
        store.pending = release(11, notes = "Фильтр по счёту")
        assertEquals(release(11, notes = "Фильтр по счёту"), whatsNew())
        assertNull(store.pending)
        assertNull(whatsNew())
    }

    @Test
    fun `keeps the release while the install has not happened`() = runTest {
        store.pending = release(12)
        assertNull(whatsNew())
        assertEquals(release(12), store.pending)
    }

    @Test
    fun `forgets a release older than the installed one without showing it`() = runTest {
        store.pending = release(9)
        assertNull(whatsNew())
        assertNull(store.pending)
    }

    @Test
    fun `blank notes are not shown`() = runTest {
        store.pending = release(11, notes = " ")
        assertNull(whatsNew())
        assertNull(store.pending)
    }
}

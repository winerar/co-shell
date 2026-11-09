package app.coshell.update

import app.coshell.core.domain.update.CheckForUpdateUseCase
import app.coshell.core.domain.update.InstalledVersion
import app.coshell.core.domain.update.WhatsNewUseCase
import app.coshell.testing.MainDispatcherRule
import app.coshell.update.startup.StartupUpdateViewModel
import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class StartupUpdateViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val source = FakeReleaseSource()
    private val store = FakeUpdateStateStore()
    private val installed = InstalledVersion(11, "0.1.11")
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-08T10:00:00Z")
    }

    private fun vm() = StartupUpdateViewModel(
        CheckForUpdateUseCase(source, store, installed, clock),
        WhatsNewUseCase(store, installed),
    )

    @Test
    fun `offers a newer release once per launch`() {
        source.release = release(12)
        val vm = vm()
        vm.onAppStart()
        assertEquals(release(12), vm.state.value.available)
        vm.dismissAvailable()
        vm.onAppStart()
        assertNull(vm.state.value.available)
    }

    @Test
    fun `next launch offers the known release again without a network check`() {
        source.release = release(12)
        vm().onAppStart()
        source.error = java.io.IOException("offline")
        val next = vm()
        next.onAppStart()
        assertEquals(release(12), next.state.value.available)
    }

    @Test
    fun `release found on the update screen is offered on the next launch`() {
        source.release = release(12)
        kotlinx.coroutines.runBlocking { CheckForUpdateUseCase(source, store, installed, clock)(force = true) }
        val vm = vm()
        vm.onAppStart()
        assertEquals(release(12), vm.state.value.available)
    }

    @Test
    fun `a newer release found later in the same launch is offered`() {
        source.release = release(12)
        val vm = vm()
        vm.onAppStart()
        vm.dismissAvailable()
        source.release = release(13)
        vm.onAppStart() // checked recently -> known 12, already offered
        assertNull(vm.state.value.available)
        store.lastCheck = null
        vm.onAppStart()
        assertEquals(release(13), vm.state.value.available)
    }

    @Test
    fun `shows what is new after an update`() {
        store.pending = release(11, notes = "Фильтр по счёту")
        val vm = vm()
        assertEquals(release(11, notes = "Фильтр по счёту"), vm.state.value.whatsNew)
        vm.dismissWhatsNew()
        assertNull(vm.state.value.whatsNew)
    }
}

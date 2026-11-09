package app.coshell.update

import app.coshell.core.domain.update.CheckForUpdateUseCase
import app.coshell.core.domain.update.InstalledVersion
import app.coshell.core.domain.update.WhatsNewUseCase
import app.coshell.testing.MainDispatcherRule
import app.coshell.update.install.InstallEvents
import app.coshell.update.notice.UpdateNoticeViewModel
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateNoticeViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val source = FakeReleaseSource()
    private val store = FakeUpdateStateStore()
    private val installer = FakeApkInstaller()
    private val installed = InstalledVersion(11, "0.1.11")
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-08T10:00:00Z")
    }

    private fun controller() = UpdateController(
        CheckForUpdateUseCase(source, store, installed, clock),
        store,
        FakeApkDownloader(),
        installer,
        InstallEvents(),
        CoroutineScope(UnconfinedTestDispatcher()),
    )

    private fun vm(controller: UpdateController = controller()) =
        UpdateNoticeViewModel(controller, WhatsNewUseCase(store, installed))

    @Test
    fun `newer release shows the pill and the badge, no dialog`() {
        source.release = release(12)
        val vm = vm()
        vm.startWatching()
        assertEquals(release(12), vm.pill.value)
        assertEquals(release(12), vm.available.value)
        vm.stopWatching()
    }

    @Test
    fun `up to date shows nothing`() {
        source.release = release(11)
        val vm = vm()
        vm.startWatching()
        assertNull(vm.pill.value)
        assertNull(vm.available.value)
    }

    @Test
    fun `dismissed pill stays hidden this launch, the badge stays, a newer release shows it again`() {
        source.release = release(12)
        val controller = controller()
        val vm = vm(controller)
        vm.startWatching()
        vm.dismissPill(release(12))
        assertNull(vm.pill.value)
        assertEquals(release(12), vm.available.value)

        source.release = release(13)
        store.lastCheck = null
        vm.startWatching()
        assertEquals(release(13), vm.pill.value)
    }

    @Test
    fun `next launch offers the known release again without a network check`() {
        source.release = release(12)
        vm().startWatching()
        source.error = IOException("offline")
        val next = vm()
        next.startWatching()
        assertEquals(release(12), next.pill.value)
    }

    @Test
    fun `tap starts the download and the pill hides while it runs`() {
        source.release = release(12)
        installer.allowed = false // stays on «Available» with a permission request: pill still makes sense
        val controller = controller()
        val vm = vm(controller)
        vm.startWatching()
        installer.allowed = true
        vm.startUpdate(release(12))
        assertEquals(UpdatePhase.Installing(release(12)), controller.phase.value)
        assertNull(vm.pill.value)
    }

    @Test
    fun `shows what is new after an update`() {
        store.pending = release(11, notes = "Фильтр по счёту")
        val vm = vm()
        assertEquals(release(11, notes = "Фильтр по счёту"), vm.whatsNew.value)
        vm.dismissWhatsNew()
        assertNull(vm.whatsNew.value)
    }
}

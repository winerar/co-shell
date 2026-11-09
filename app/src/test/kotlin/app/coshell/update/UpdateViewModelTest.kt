package app.coshell.update

import app.coshell.core.domain.update.CheckForUpdateUseCase
import app.coshell.core.domain.update.InstalledVersion
import app.coshell.testing.MainDispatcherRule
import app.coshell.update.install.InstallEvents
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val source = FakeReleaseSource()
    private val store = FakeUpdateStateStore()
    private val installer = FakeApkInstaller()
    private val installed = InstalledVersion(10, "0.1.0")
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-08T10:00:00Z")
    }
    private val controller = UpdateController(
        CheckForUpdateUseCase(source, store, installed, clock),
        store,
        FakeApkDownloader(),
        installer,
        InstallEvents(),
        CoroutineScope(UnconfinedTestDispatcher()),
    )

    private fun vm() = UpdateViewModel(controller, installed)

    @Test
    fun `opening the screen checks even right after an automatic check`() {
        store.lastCheck = clock.now()
        source.release = release(11)
        val vm = vm()
        assertEquals(UpdatePhase.Available(release(11)), vm.state.value.phase)
        assertEquals("0.1.0", vm.state.value.installedVersionName)
    }

    @Test
    fun `up to date, not configured and failed check`() {
        source.release = release(10)
        assertEquals(UpdatePhase.UpToDate, vm().state.value.phase)
        source.release = null
        assertEquals(UpdatePhase.NotConfigured, vm().state.value.phase)
        source.error = IOException("offline")
        assertEquals(UpdatePhase.Failed(UpdateError.Check), vm().state.value.phase)
    }

    @Test
    fun `screen opened from the pill shows the running download, not a new check`() {
        source.release = release(12)
        controller.download(release(11))
        assertEquals(UpdatePhase.Installing(release(11)), vm().state.value.phase)
    }

    @Test
    fun `permission request shows on the screen`() {
        source.release = release(11)
        installer.allowed = false
        val vm = vm()
        vm.downloadAndInstall()
        assertTrue(vm.state.value.needsInstallPermission)
    }
}

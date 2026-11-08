package app.coshell.update

import app.coshell.core.domain.update.CheckForUpdateUseCase
import app.coshell.core.domain.update.InstalledVersion
import app.coshell.testing.MainDispatcherRule
import app.coshell.update.data.ChecksumMismatchException
import app.coshell.update.install.InstallEvents
import app.coshell.update.install.InstallOutcome
import java.io.File
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class UpdateViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val source = FakeReleaseSource()
    private val store = FakeUpdateStateStore()
    private val downloader = FakeApkDownloader()
    private val installer = FakeApkInstaller()
    private val events = InstallEvents()
    private val installed = InstalledVersion(10, "0.1.0")
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-08T10:00:00Z")
    }

    private fun vm() = UpdateViewModel(
        CheckForUpdateUseCase(source, store, installed, clock),
        store,
        downloader,
        installer,
        events,
        installed,
    )

    @Test
    fun `opening the screen checks even right after an automatic check`() {
        store.lastCheck = clock.now()
        source.release = release(11)
        assertEquals(UpdatePhase.Available(release(11)), vm().state.value.phase)
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
    fun `download remembers the release and starts the install`() {
        source.release = release(11)
        val vm = vm()
        vm.downloadAndInstall()
        assertEquals(UpdatePhase.Installing(release(11)), vm.state.value.phase)
        assertEquals(release(11), store.pending)
        assertEquals(listOf(File("co-shell-11.apk")), installer.installed)
    }

    @Test
    fun `asks for the install permission before downloading`() {
        source.release = release(11)
        installer.allowed = false
        val vm = vm()
        vm.downloadAndInstall()
        assertTrue(vm.state.value.needsInstallPermission)
        assertTrue(downloader.downloaded.isEmpty())

        vm.openInstallPermissionSettings()
        assertEquals(1, installer.settingsOpened)
        installer.allowed = true
        vm.refreshInstallPermission()
        assertEquals(false, vm.state.value.needsInstallPermission)
    }

    @Test
    fun `checksum mismatch is not installed`() {
        source.release = release(11)
        downloader.error = ChecksumMismatchException()
        val vm = vm()
        vm.downloadAndInstall()
        assertEquals(UpdatePhase.Failed(UpdateError.Checksum, release(11)), vm.state.value.phase)
        assertTrue(installer.installed.isEmpty())
        assertEquals(null, store.pending)
    }

    @Test
    fun `network error during download can be retried`() {
        source.release = release(11)
        downloader.error = IOException("reset")
        val vm = vm()
        vm.downloadAndInstall()
        assertEquals(UpdatePhase.Failed(UpdateError.Download, release(11)), vm.state.value.phase)
        downloader.error = null
        vm.downloadAndInstall()
        assertEquals(UpdatePhase.Installing(release(11)), vm.state.value.phase)
    }

    @Test
    fun `cancelled install returns to the offer, failed install shows an error`() {
        source.release = release(11)
        val vm = vm()
        vm.downloadAndInstall()
        events.emit(InstallOutcome.Cancelled)
        assertEquals(UpdatePhase.Available(release(11)), vm.state.value.phase)
        vm.downloadAndInstall()
        events.emit(InstallOutcome.Failed("conflict"))
        assertEquals(UpdatePhase.Failed(UpdateError.Install, release(11)), vm.state.value.phase)
    }
}

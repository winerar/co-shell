package app.coshell.update

import app.coshell.core.domain.update.CheckForUpdateUseCase
import app.coshell.core.domain.update.InstalledVersion
import app.coshell.update.data.ChecksumMismatchException
import app.coshell.update.install.InstallEvents
import app.coshell.update.install.InstallOutcome
import java.io.File
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateControllerTest {
    private val source = FakeReleaseSource()
    private val store = FakeUpdateStateStore()
    private val downloader = FakeApkDownloader()
    private val installer = FakeApkInstaller()
    private val events = InstallEvents()
    private val installed = InstalledVersion(10, "0.1.0")
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-08T10:00:00Z")
    }

    private fun controller() = UpdateController(
        CheckForUpdateUseCase(source, store, installed, clock),
        store,
        downloader,
        installer,
        events,
        CoroutineScope(UnconfinedTestDispatcher()),
    )

    @Test
    fun `check reports the phase and remembers the available release`() {
        source.release = release(11)
        val c = controller()
        c.checkNow()
        assertEquals(UpdatePhase.Available(release(11)), c.phase.value)
        assertEquals(release(11), c.available.value)
        source.release = release(10)
        c.checkNow()
        assertEquals(UpdatePhase.UpToDate, c.phase.value)
        assertNull(c.available.value)
    }

    @Test
    fun `automatic check goes to the network at most once per interval`() = runBlocking {
        source.release = release(11)
        val c = controller()
        c.autoCheck()
        assertEquals(release(11), c.available.value)
        source.error = IOException("offline")
        c.autoCheck() // checked recently: the known release again, no network
        assertEquals(release(11), c.available.value)
    }

    @Test
    fun `download from the pill skips the screen check and starts the install`() {
        val c = controller()
        c.download(release(11))
        assertEquals(UpdatePhase.Installing(release(11)), c.phase.value)
        assertEquals(release(11), store.pending)
        assertEquals(listOf(File("co-shell-11.apk")), installer.installed)
    }

    @Test
    fun `no check while a download or install is under way`() {
        source.release = release(12)
        val c = controller()
        c.download(release(11))
        c.checkNow()
        assertEquals(UpdatePhase.Installing(release(11)), c.phase.value)
    }

    @Test
    fun `asks for the install permission before downloading`() {
        installer.allowed = false
        val c = controller()
        c.download(release(11))
        assertTrue(c.needsInstallPermission.value)
        assertEquals(UpdatePhase.Available(release(11)), c.phase.value)
        assertTrue(downloader.downloaded.isEmpty())

        c.openInstallPermissionSettings()
        assertEquals(1, installer.settingsOpened)
        installer.allowed = true
        c.refreshInstallPermission()
        assertEquals(false, c.needsInstallPermission.value)
    }

    @Test
    fun `checksum mismatch is not installed`() {
        downloader.error = ChecksumMismatchException()
        val c = controller()
        c.download(release(11))
        assertEquals(UpdatePhase.Failed(UpdateError.Checksum, release(11)), c.phase.value)
        assertTrue(installer.installed.isEmpty())
        assertEquals(null, store.pending)
    }

    @Test
    fun `network error during download can be retried`() {
        downloader.error = IOException("reset")
        val c = controller()
        c.download(release(11))
        assertEquals(UpdatePhase.Failed(UpdateError.Download, release(11)), c.phase.value)
        downloader.error = null
        c.download()
        assertEquals(UpdatePhase.Installing(release(11)), c.phase.value)
    }

    @Test
    fun `cancelled install returns to the offer, failed install shows an error`() {
        val c = controller()
        c.download(release(11))
        events.emit(InstallOutcome.Cancelled)
        assertEquals(UpdatePhase.Available(release(11)), c.phase.value)
        c.download()
        events.emit(InstallOutcome.Failed("conflict"))
        assertEquals(UpdatePhase.Failed(UpdateError.Install, release(11)), c.phase.value)
    }
}

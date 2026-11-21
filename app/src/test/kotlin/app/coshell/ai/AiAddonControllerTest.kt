package app.coshell.ai

import app.coshell.core.domain.update.AppRelease
import app.coshell.update.install.InstallOutcome
import java.io.File
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Turning «ИИ на телефоне» on and off (#587): step 1 installs the add-on, step 2 downloads the model; every way it can stop. */
@OptIn(ExperimentalCoroutinesApi::class)
class AiAddonControllerTest {
    private class FakeControl(var installed: Boolean = false, var model: Int = AddonProtocol.MODEL_MISSING) : AddonControl {
        var download: () -> Flow<ModelProgress> = { flow { emit(ModelProgress(10, 100)); model = AddonProtocol.MODEL_READY } }
        var cancelled = 0
        var deleted = 0
        override suspend fun status() = AddonStatus(installed, model)
        override fun downloadModel() = download()
        override suspend fun cancelDownload() { cancelled++ }
        override suspend fun deleteModel() { deleted++; model = AddonProtocol.MODEL_MISSING }
    }

    private class FakePackage(val control: FakeControl, var release: AppRelease? = RELEASE, var failDownload: Boolean = false, var installsItself: Boolean = true) : AddonPackage {
        val installedFiles = mutableListOf<File>()
        override suspend fun latest() = release
        override suspend fun download(release: AppRelease, onProgress: (Float) -> Unit): File {
            if (failDownload) throw IOException("offline")
            onProgress(0.5f)
            return File("addon.apk")
        }
        override suspend fun install(file: File) {
            installedFiles += file
            if (installsItself) control.installed = true
        }
    }

    private val outcomes = MutableSharedFlow<InstallOutcome>(extraBufferCapacity = 1)

    private var keepAliveCalls = 0

    private fun TestScope.controller(control: FakeControl, pkg: AddonPackage) =
        AiAddonController(control, pkg, outcomes, this, pollMs = 100, installWaitMs = 10_000, keepAlive = { keepAliveCalls++ })

    @Test
    fun `nothing installed - installs the add-on, downloads the model, ends on`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeControl()
        val pkg = FakePackage(control)
        val controller = controller(control, pkg)
        controller.enable()
        advanceTimeBy(1_000); runCurrent()
        assertEquals(1, pkg.installedFiles.size)
        assertEquals(AiAddonState(AiAddonPhase.On), controller.state.value)
    }

    @Test
    fun `add-on already there - only the model comes`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeControl(installed = true)
        val pkg = FakePackage(control)
        val controller = controller(control, pkg)
        controller.enable()
        runCurrent()
        assertTrue(pkg.installedFiles.isEmpty())
        assertEquals(AiAddonPhase.On, controller.state.value.phase)
    }

    @Test
    fun `no published add-on file - says so and stays off`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeControl()
        val controller = controller(control, FakePackage(control, release = null))
        controller.enable()
        runCurrent()
        assertEquals(AiAddonState(AiAddonPhase.Off, AiAddonProblem.NO_ADDON_FILE), controller.state.value)
    }

    @Test
    fun `add-on file does not download - download problem`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeControl()
        val controller = controller(control, FakePackage(control, failDownload = true))
        controller.enable()
        runCurrent()
        assertEquals(AiAddonProblem.DOWNLOAD_FAILED, controller.state.value.problem)
    }

    @Test
    fun `person closes Android's install dialog - cancelled, nothing installed`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeControl()
        val controller = controller(control, FakePackage(control, installsItself = false))
        controller.enable()
        runCurrent()
        assertEquals(AiAddonPhase.InstallingAddon(null), controller.state.value.phase)
        outcomes.tryEmit(InstallOutcome.Cancelled)
        advanceTimeBy(500); runCurrent()
        assertEquals(AiAddonState(AiAddonPhase.Off, AiAddonProblem.INSTALL_CANCELLED), controller.state.value)
    }

    @Test
    fun `install never finishes - install problem after the wait`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeControl()
        val controller = controller(control, FakePackage(control, installsItself = false))
        controller.enable()
        advanceTimeBy(11_000); runCurrent()
        assertEquals(AiAddonProblem.INSTALL_FAILED, controller.state.value.problem)
    }

    @Test
    fun `model download fails - download problem, off`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeControl(installed = true).apply { download = { flow { emit(ModelProgress(5, 100)); throw IOException("reset") } } }
        val controller = controller(control, FakePackage(control))
        controller.enable()
        runCurrent()
        assertEquals(AiAddonState(AiAddonPhase.Off, AiAddonProblem.DOWNLOAD_FAILED), controller.state.value)
    }

    @Test
    fun `cancel stops the download and settles on what the phone has`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeControl(installed = true).apply { download = { flow { emit(ModelProgress(5, 100)); kotlinx.coroutines.awaitCancellation() } } }
        val controller = controller(control, FakePackage(control))
        controller.enable()
        runCurrent()
        assertEquals(AiAddonPhase.DownloadingModel(5, 100), controller.state.value.phase)
        controller.cancel()
        runCurrent()
        assertEquals(1, control.cancelled)
        assertEquals(AiAddonState(AiAddonPhase.Off), controller.state.value)
    }

    @Test
    fun `delete removes the model and leaves the add-on`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeControl(installed = true, model = AddonProtocol.MODEL_READY)
        val controller = controller(control, FakePackage(control))
        controller.refresh()
        assertEquals(AiAddonPhase.On, controller.state.value.phase)
        controller.deleteModel()
        runCurrent()
        assertEquals(1, control.deleted)
        assertEquals(AiAddonPhase.Off, controller.state.value.phase)
        assertTrue(control.installed)
        assertNull(controller.state.value.problem)
    }

    @Test
    fun `turning on starts the foreground keep-alive once`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeControl(installed = true).apply { download = { flow { emit(ModelProgress(1, 10)); kotlinx.coroutines.awaitCancellation() } } }
        val controller = controller(control, FakePackage(control))
        controller.enable()
        controller.enable()
        runCurrent()
        assertEquals(1, keepAliveCalls)
        controller.cancel()
        runCurrent()
    }

    @Test
    fun `a second enable while one runs is ignored`() = runTest(UnconfinedTestDispatcher()) {
        val control = FakeControl(installed = true).apply { download = { flow { emit(ModelProgress(1, 10)); kotlinx.coroutines.awaitCancellation() } } }
        val controller = controller(control, FakePackage(control))
        controller.enable()
        controller.enable()
        runCurrent()
        assertEquals(AiAddonPhase.DownloadingModel(1, 10), controller.state.value.phase)
        controller.cancel() // the fake download never ends by itself
        runCurrent()
    }

    private companion object {
        val RELEASE = AppRelease(1, "0.1", "https://example.org/ai-addon.apk", "0".repeat(64), 20_000_000, "")
    }
}

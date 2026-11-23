package app.coshell.ai

import app.coshell.update.install.InstallOutcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Where «ИИ на телефоне» stands (#587); the screen and the «Настройки» row both show it. */
sealed interface AiAddonPhase {
    /** Nothing installed, or the model is gone. */
    data object Off : AiAddonPhase

    /**
     * Step 1: [progress] of the add-on's APK (0..1) while it downloads, `null` while Android asks the person to install it;
     * [total] — the APK's size in bytes (`0` — not known), for «3 из 20 МБ».
     */
    data class InstallingAddon(val progress: Float?, val total: Long = 0) : AiAddonPhase

    /** Step 2: the model is coming ([total] is `0` until known). */
    data class DownloadingModel(val done: Long, val total: Long) : AiAddonPhase

    data object On : AiAddonPhase
}

enum class AiAddonProblem { NO_ADDON_FILE, INSTALL_CANCELLED, INSTALL_FAILED, DOWNLOAD_FAILED }

data class AiAddonState(val phase: AiAddonPhase = AiAddonPhase.Off, val problem: AiAddonProblem? = null)

/**
 * One per process (a download keeps going when the screen is closed): turns the add-on and its model on and off.
 * «Включить» = step 1 (install the add-on, if it is not there) then step 2 (download the model); «Отменить» stops either;
 * «Удалить модель» frees the 2,5 GB and leaves the add-on (poll 2026-11-21, all «А»).
 */
class AiAddonController(
    private val control: AddonControl,
    private val addon: AddonPackage,
    private val installOutcomes: Flow<InstallOutcome>,
    private val scope: CoroutineScope,
    private val pollMs: Long = 1_000,
    private val installWaitMs: Long = 5 * 60_000,
    /** Starts what keeps the process alive while the work runs (a foreground service with a notification); nothing in tests. */
    private val keepAlive: () -> Unit = {},
) {
    private val mutable = MutableStateFlow(AiAddonState())
    val state: StateFlow<AiAddonState> = mutable.asStateFlow()
    private var job: Job? = null

    /** Looks at the phone: add-on installed? model downloaded? Does not disturb a running turn-on. */
    suspend fun refresh() {
        if (job?.isActive == true) return
        settle()
    }

    private suspend fun settle() {
        val status = control.status()
        mutable.update { it.copy(phase = if (status.ready) AiAddonPhase.On else AiAddonPhase.Off) }
    }

    fun enable() {
        if (job?.isActive == true) return
        mutable.value = AiAddonState()
        keepAlive()
        job = scope.launch { runCatching { turnOn() }.onFailure { if (it !is kotlinx.coroutines.CancellationException) fail(AiAddonProblem.DOWNLOAD_FAILED) } }
    }

    fun cancel() {
        job?.cancel()
        scope.launch {
            control.cancelDownload()
            mutable.update { it.copy(problem = null) }
            settle()
        }
    }

    fun deleteModel() {
        job?.cancel()
        scope.launch {
            control.deleteModel()
            mutable.value = AiAddonState()
            settle()
        }
    }

    private suspend fun turnOn() {
        if (!control.status().installed && !installAddon()) return
        mutable.value = AiAddonState(AiAddonPhase.DownloadingModel(0, 0))
        try {
            control.downloadModel().collect { p -> mutable.value = AiAddonState(AiAddonPhase.DownloadingModel(p.done, p.total)) }
        } catch (e: java.io.IOException) {
            return fail(AiAddonProblem.DOWNLOAD_FAILED)
        }
        mutable.value = AiAddonState(if (control.status().ready) AiAddonPhase.On else AiAddonPhase.Off)
    }

    /** Step 1. `false` — it did not work out (the problem is set); `true` — the add-on is installed. */
    private suspend fun installAddon(): Boolean {
        mutable.value = AiAddonState(AiAddonPhase.InstallingAddon(0f))
        val release = addon.latest() ?: return fail(AiAddonProblem.NO_ADDON_FILE).let { false }
        val file = try {
            addon.download(release) { p -> mutable.value = AiAddonState(AiAddonPhase.InstallingAddon(p, release.sizeBytes)) }
        } catch (e: java.io.IOException) {
            return fail(AiAddonProblem.DOWNLOAD_FAILED).let { false }
        }
        mutable.value = AiAddonState(AiAddonPhase.InstallingAddon(null))
        addon.install(file)
        // Whichever comes first: the add-on answers (installed), or Android reports that the dialog was closed or failed.
        return when (waitForInstall()) {
            InstallEnd.DONE -> true
            InstallEnd.CANCELLED -> fail(AiAddonProblem.INSTALL_CANCELLED).let { false }
            InstallEnd.TIMEOUT -> fail(AiAddonProblem.INSTALL_FAILED).let { false }
        }
    }

    private enum class InstallEnd { DONE, CANCELLED, TIMEOUT }

    private suspend fun waitForInstall(): InstallEnd = withTimeoutOrNull(installWaitMs) {
        coroutineScope {
            val outcome = async { installOutcomes.first() }
            while (true) {
                if (control.status().installed) { outcome.cancel(); return@coroutineScope InstallEnd.DONE }
                if (outcome.isCompleted) return@coroutineScope InstallEnd.CANCELLED
                delay(pollMs)
            }
            @Suppress("UNREACHABLE_CODE")
            InstallEnd.TIMEOUT
        }
    } ?: InstallEnd.TIMEOUT

    private fun fail(problem: AiAddonProblem) {
        mutable.value = AiAddonState(AiAddonPhase.Off, problem)
    }
}

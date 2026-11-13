package app.coshell.update

import app.coshell.core.domain.update.AppRelease
import app.coshell.core.domain.update.CheckForUpdateUseCase
import app.coshell.core.domain.update.UpdateCheckResult
import app.coshell.core.domain.update.UpdateStateStore
import app.coshell.update.data.ApkDownloader
import app.coshell.update.data.ChecksumMismatchException
import app.coshell.update.install.ApkInstaller
import app.coshell.update.install.InstallEvents
import app.coshell.update.install.InstallOutcome
import app.coshell.update.log.NoUpdateJournal
import app.coshell.update.log.UpdateJournal
import java.io.File
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns the self-update for the whole process: check, download, verify, install. It lives longer than
 * the update screen, so a download started from the update pill (or the screen) keeps going when the
 * user leaves the screen, and the screen shows the same progress when opened again.
 */
class UpdateController(
    private val check: CheckForUpdateUseCase,
    private val store: UpdateStateStore,
    private val downloader: ApkDownloader,
    private val installer: ApkInstaller,
    installEvents: InstallEvents,
    private val scope: CoroutineScope,
    private val journal: UpdateJournal = NoUpdateJournal,
) {
    /** The verified APK of the current attempt and its release, for «Установить вручную». */
    private var apk: Pair<AppRelease, File>? = null

    private val _phase = MutableStateFlow<UpdatePhase>(UpdatePhase.Checking)
    val phase: StateFlow<UpdatePhase> = _phase.asStateFlow()

    /** A newer release known from the last check (automatic or not); drives the update pill and badges. */
    private val _available = MutableStateFlow<AppRelease?>(null)
    val available: StateFlow<AppRelease?> = _available.asStateFlow()

    /** The user has not yet allowed co-shell to install apps; the update screen asks for it. */
    private val _needsInstallPermission = MutableStateFlow(false)
    val needsInstallPermission: StateFlow<Boolean> = _needsInstallPermission.asStateFlow()

    init {
        scope.launch {
            installEvents.outcomes.collect { outcome ->
                val release = _phase.value.release ?: return@collect
                _phase.value = when (outcome) {
                    InstallOutcome.Cancelled -> UpdatePhase.Available(release)
                    is InstallOutcome.Failed -> UpdatePhase.Failed(UpdateError.Install, release)
                }
            }
        }
    }

    /** A download or install is under way: a new check would hide its progress. */
    val busy: Boolean
        get() = _phase.value.let { it is UpdatePhase.Downloading || it is UpdatePhase.Installing }

    /**
     * The quiet check while the app is open: goes to the network at most once per
     * [CheckForUpdateUseCase.interval], otherwise reports the release found last time.
     */
    suspend fun autoCheck() {
        when (val result = check()) {
            is UpdateCheckResult.Available -> _available.value = result.release
            is UpdateCheckResult.UpToDate -> _available.value = null
            else -> Unit
        }
    }

    /** A check the user asked for (opening the screen, «Проверить»): always goes to the network. */
    fun checkNow() {
        if (busy) return
        _phase.value = UpdatePhase.Checking
        scope.launch {
            val result = check(force = true)
            when (result) {
                is UpdateCheckResult.Available -> _available.value = result.release
                is UpdateCheckResult.UpToDate -> _available.value = null
                else -> Unit
            }
            _phase.value = when (result) {
                is UpdateCheckResult.Available -> UpdatePhase.Available(result.release)
                is UpdateCheckResult.UpToDate -> UpdatePhase.UpToDate(result.current)
                is UpdateCheckResult.CheckedRecently -> UpdatePhase.UpToDate(result.current)
                UpdateCheckResult.NotConfigured -> UpdatePhase.NotConfigured
                is UpdateCheckResult.Failed -> UpdatePhase.Failed(UpdateError.Check)
            }
        }
    }

    /** Called when the update screen becomes visible again, e.g. back from the permission settings. */
    fun refreshInstallPermission() {
        _needsInstallPermission.update { it && !installer.canInstall() }
    }

    fun openInstallPermissionSettings() = installer.openInstallPermissionSettings()

    /**
     * Downloads [release] (by default the one on screen) and opens the system install dialog. Without
     * the install permission nothing is downloaded and [needsInstallPermission] turns on.
     */
    fun download(release: AppRelease? = _phase.value.release) {
        if (release == null || _phase.value is UpdatePhase.Downloading) return
        if (!installer.canInstall()) {
            _phase.value = UpdatePhase.Available(release)
            _needsInstallPermission.value = true
            return
        }
        _needsInstallPermission.value = false
        _phase.value = UpdatePhase.Downloading(release, 0f)
        journal.add("download ${release.versionCode} started")
        scope.launch {
            val file = try {
                var lastPercent = -1
                downloader.download(release) { progress ->
                    val percent = (progress * 100).toInt()
                    if (percent != lastPercent) {
                        lastPercent = percent
                        _phase.value = UpdatePhase.Downloading(release, progress)
                    }
                }
            } catch (e: ChecksumMismatchException) {
                journal.add("download failed: checksum")
                return@launch run { _phase.value = UpdatePhase.Failed(UpdateError.Checksum, release) }
            } catch (e: IOException) {
                journal.add("download failed: ${e.message}")
                return@launch run { _phase.value = UpdatePhase.Failed(UpdateError.Download, release) }
            }
            journal.add("downloaded ${file.length()} bytes, checksum ok")
            apk = release to file
            store.setPendingRelease(release)
            _phase.value = UpdatePhase.Installing(release)
            if (journal.preferManualInstall) return@launch openInstaller(release, file)
            try {
                installer.install(file)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                journal.add("session failed: ${e::class.simpleName} ${e.message}")
                _phase.value = UpdatePhase.Failed(UpdateError.Install, release)
            }
        }
    }

    /**
     * «Установить вручную» (offered right away, #269): the Android installer with the downloaded APK;
     * remembered for next time. Before the download (under «Обновить») it downloads, then opens the installer.
     */
    fun installManually() {
        val release = _phase.value.release ?: return
        if (_phase.value is UpdatePhase.Downloading) return
        journal.preferManualInstall = true
        // No APK in this process (the app was restarted): download again, it then opens the installer.
        val file = apk?.takeIf { it.first == release }?.second ?: return download(release)
        openInstaller(release, file)
    }

    private fun openInstaller(release: AppRelease, file: File) {
        // Android tells nothing back here, so the button stays: a closed installer can be opened again.
        _phase.value = UpdatePhase.Installing(release)
        journal.add("manual install opened")
        try {
            installer.installManually(file)
        } catch (e: IllegalArgumentException) {
            journal.add("manual install failed: ${e.message}")
            _phase.value = UpdatePhase.Failed(UpdateError.Install, release)
        }
    }
}

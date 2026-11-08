package app.coshell.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.update.CheckForUpdateUseCase
import app.coshell.core.domain.update.InstalledVersion
import app.coshell.core.domain.update.UpdateCheckResult
import app.coshell.core.domain.update.UpdateStateStore
import app.coshell.update.data.ApkDownloader
import app.coshell.update.data.ChecksumMismatchException
import app.coshell.update.install.ApkInstaller
import app.coshell.update.install.InstallEvents
import app.coshell.update.install.InstallOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Checks for a new build on open, then downloads, verifies and installs it on request. */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val check: CheckForUpdateUseCase,
    private val store: UpdateStateStore,
    private val downloader: ApkDownloader,
    private val installer: ApkInstaller,
    installEvents: InstallEvents,
    installed: InstalledVersion,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UpdateUiState(installedVersionName = installed.versionName, installedVersionCode = installed.versionCode),
    )
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()

    init {
        checkNow()
        viewModelScope.launch {
            installEvents.outcomes.collect { outcome ->
                val release = _state.value.phase.release ?: return@collect
                setPhase(
                    when (outcome) {
                        InstallOutcome.Cancelled -> UpdatePhase.Available(release)
                        is InstallOutcome.Failed -> UpdatePhase.Failed(UpdateError.Install, release)
                    },
                )
            }
        }
    }

    fun checkNow() {
        setPhase(UpdatePhase.Checking)
        viewModelScope.launch {
            setPhase(
                when (val result = check(force = true)) {
                    is UpdateCheckResult.Available -> UpdatePhase.Available(result.release)
                    UpdateCheckResult.UpToDate, UpdateCheckResult.CheckedRecently -> UpdatePhase.UpToDate
                    UpdateCheckResult.NotConfigured -> UpdatePhase.NotConfigured
                    is UpdateCheckResult.Failed -> UpdatePhase.Failed(UpdateError.Check)
                },
            )
        }
    }

    /** Called when the screen becomes visible again, e.g. back from the permission settings. */
    fun refreshInstallPermission() {
        _state.update { it.copy(needsInstallPermission = it.needsInstallPermission && !installer.canInstall()) }
    }

    fun openInstallPermissionSettings() = installer.openInstallPermissionSettings()

    fun downloadAndInstall() {
        val phase = _state.value.phase
        val release = phase.release ?: return
        if (phase is UpdatePhase.Downloading) return
        if (!installer.canInstall()) {
            _state.update { it.copy(needsInstallPermission = true) }
            return
        }
        _state.update { it.copy(needsInstallPermission = false) }
        setPhase(UpdatePhase.Downloading(release, 0f))
        viewModelScope.launch {
            val file = try {
                var lastPercent = -1
                downloader.download(release) { progress ->
                    val percent = (progress * 100).toInt()
                    if (percent != lastPercent) {
                        lastPercent = percent
                        setPhase(UpdatePhase.Downloading(release, progress))
                    }
                }
            } catch (e: ChecksumMismatchException) {
                return@launch setPhase(UpdatePhase.Failed(UpdateError.Checksum, release))
            } catch (e: IOException) {
                return@launch setPhase(UpdatePhase.Failed(UpdateError.Download, release))
            }
            store.setPendingRelease(release)
            setPhase(UpdatePhase.Installing(release))
            try {
                installer.install(file)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setPhase(UpdatePhase.Failed(UpdateError.Install, release))
            }
        }
    }

    private fun setPhase(phase: UpdatePhase) = _state.update { it.copy(phase = phase) }
}

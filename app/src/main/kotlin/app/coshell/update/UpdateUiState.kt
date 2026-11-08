package app.coshell.update

import app.coshell.core.domain.update.AppRelease

/** State of "More → App update". */
data class UpdateUiState(
    /** Installed version for display, e.g. "0.1.0 (сборка 42)". */
    val installedVersionName: String = "",
    val installedVersionCode: Int = 0,
    val phase: UpdatePhase = UpdatePhase.Checking,
    /** The user has not yet allowed co-shell to install apps; shown before downloading. */
    val needsInstallPermission: Boolean = false,
)

sealed interface UpdatePhase {
    data object Checking : UpdatePhase
    data object UpToDate : UpdatePhase
    data object NotConfigured : UpdatePhase
    data class Available(val release: AppRelease) : UpdatePhase
    /** [progress] is 0..1. */
    data class Downloading(val release: AppRelease, val progress: Float) : UpdatePhase
    /** The system install dialog is open (or about to be). */
    data class Installing(val release: AppRelease) : UpdatePhase
    /** [release] is set when the failure happened after the check, so "retry" downloads again. */
    data class Failed(val error: UpdateError, val release: AppRelease? = null) : UpdatePhase
}

enum class UpdateError { Check, Download, Checksum, Install }

/** Release the screen is working with, if any. */
internal val UpdatePhase.release: AppRelease?
    get() = when (this) {
        is UpdatePhase.Available -> release
        is UpdatePhase.Downloading -> release
        is UpdatePhase.Installing -> release
        is UpdatePhase.Failed -> release
        else -> null
    }

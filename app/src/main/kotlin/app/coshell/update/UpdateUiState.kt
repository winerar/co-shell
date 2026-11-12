package app.coshell.update

import app.coshell.core.domain.update.AppRelease
import kotlin.time.Instant

/** State of "More → App update". */
data class UpdateUiState(
    /** Installed version for display: «Версия 0.37 · сборка 400» (#268). */
    val installedVersionName: String = "",
    val installedVersionCode: Int = 0,
    /** When the installed build was installed (Android's last update time), for «Ваша версия». */
    val installedAt: Instant? = null,
    val phase: UpdatePhase = UpdatePhase.Checking,
    /** The user has not yet allowed co-shell to install apps; shown before downloading. */
    val needsInstallPermission: Boolean = false,
)

sealed interface UpdatePhase {
    data object Checking : UpdatePhase
    /** [current] — the installed release as published (its notes for «Что в этой версии»), if the server knows it. */
    data class UpToDate(val current: AppRelease? = null) : UpdatePhase
    data object NotConfigured : UpdatePhase
    data class Available(val release: AppRelease) : UpdatePhase
    /** [progress] is 0..1. */
    data class Downloading(val release: AppRelease, val progress: Float) : UpdatePhase
    /**
     * The system install dialog is open (or about to be). «Установить вручную» is offered right away, in case
     * the dialog never appears (Xiaomi, #217, #269).
     */
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

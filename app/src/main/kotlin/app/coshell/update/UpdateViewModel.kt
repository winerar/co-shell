package app.coshell.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.update.InstalledVersion
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * «Обновление приложения»: shows [UpdateController]'s state. Checks on open unless a download is
 * already running (e.g. started from the update pill), so its progress shows right away.
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val controller: UpdateController,
    installed: InstalledVersion,
) : ViewModel() {

    private val initial = UpdateUiState(
        installedVersionName = installed.versionName,
        installedVersionCode = installed.versionCode,
        installedAt = installed.installedAt,
    )

    val state: StateFlow<UpdateUiState> =
        combine(controller.phase, controller.needsInstallPermission) { phase, needsPermission ->
            initial.copy(phase = phase, needsInstallPermission = needsPermission)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, initial.copy(phase = controller.phase.value))

    init {
        controller.checkNow()
    }

    fun checkNow() = controller.checkNow()

    fun refreshInstallPermission() = controller.refreshInstallPermission()

    fun openInstallPermissionSettings() = controller.openInstallPermissionSettings()

    fun downloadAndInstall() = controller.download()

    fun installManually() = controller.installManually()
}

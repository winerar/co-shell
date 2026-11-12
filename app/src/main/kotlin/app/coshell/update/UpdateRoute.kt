package app.coshell.update

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [UpdateScreen] to its ViewModel. */
@Composable
fun UpdateRoute(onBack: () -> Unit, viewModel: UpdateViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshInstallPermission()
        onPauseOrDispose {}
    }
    UpdateScreen(
        state = state,
        actions = UpdateActions(
            onBack = onBack,
            onCheck = viewModel::checkNow,
            onDownload = viewModel::downloadAndInstall,
            onAllowInstall = viewModel::openInstallPermissionSettings,
            onInstallManually = viewModel::installManually,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class UpdateActions(
    val onBack: () -> Unit = {},
    val onCheck: () -> Unit = {},
    val onDownload: () -> Unit = {},
    val onAllowInstall: () -> Unit = {},
    /** «Установить вручную»: the Android installer when its dialog did not appear (#217). */
    val onInstallManually: () -> Unit = {},
)

package app.coshell.update

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [UpdateScreen] to its ViewModel. */
@Composable
fun UpdateRoute(onBack: () -> Unit, onPastReleases: () -> Unit, onFeedback: () -> Unit, viewModel: UpdateViewModel = hiltViewModel()) {
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
            onPastReleases = onPastReleases,
            onFeedback = onFeedback,
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
    /** «Прошлые версии» (#284). */
    val onPastReleases: () -> Unit = {},
    /** «Отправить отзыв» on «О приложении» (#440). */
    val onFeedback: () -> Unit = {},
)

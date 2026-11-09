package app.coshell.update.startup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAlertDialog

/** Place once at the app root; [onOpenUpdate] opens "More → App update". */
@Composable
fun StartupUpdateDialogs(onOpenUpdate: () -> Unit, viewModel: StartupUpdateViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleStartEffect(viewModel) {
        viewModel.onAppStart()
        onStopOrDispose {}
    }

    val whatsNew = state.whatsNew
    val available = state.available
    when {
        whatsNew != null -> CoShellAlertDialog(
            title = stringResource(R.string.update_whats_new_title, whatsNew.versionName),
            text = whatsNew.notes,
            confirmLabel = stringResource(R.string.update_whats_new_ok),
            onConfirm = viewModel::dismissWhatsNew,
            onDismiss = viewModel::dismissWhatsNew,
        )
        available != null -> CoShellAlertDialog(
            title = stringResource(R.string.update_available, available.versionName, available.versionCode),
            text = available.notes.ifBlank { stringResource(R.string.update_available_no_notes) },
            confirmLabel = stringResource(R.string.update_open),
            onConfirm = {
                viewModel.dismissAvailable()
                onOpenUpdate()
            },
            onDismiss = viewModel::dismissAvailable,
            dismissLabel = stringResource(R.string.update_later),
        )
    }
}

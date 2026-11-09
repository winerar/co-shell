package app.coshell.update.startup

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellTextButton

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
        whatsNew != null -> AlertDialog(
            onDismissRequest = viewModel::dismissWhatsNew,
            title = { Text(stringResource(R.string.update_whats_new_title, whatsNew.versionName)) },
            text = { Text(whatsNew.notes, Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = {
                CoShellTextButton(onClick = viewModel::dismissWhatsNew) { Text(stringResource(R.string.update_whats_new_ok)) }
            },
        )
        available != null -> AlertDialog(
            onDismissRequest = viewModel::dismissAvailable,
            title = { Text(stringResource(R.string.update_available, available.versionName, available.versionCode)) },
            text = {
                Text(
                    available.notes.ifBlank { stringResource(R.string.update_available_no_notes) },
                    Modifier.verticalScroll(rememberScrollState()),
                )
            },
            confirmButton = {
                CoShellTextButton(onClick = {
                    viewModel.dismissAvailable()
                    onOpenUpdate()
                }) { Text(stringResource(R.string.update_open)) }
            },
            dismissButton = {
                CoShellTextButton(onClick = viewModel::dismissAvailable) { Text(stringResource(R.string.update_later)) }
            },
        )
    }
}

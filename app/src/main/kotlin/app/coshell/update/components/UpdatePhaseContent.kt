package app.coshell.update.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.update.UpdateActions
import app.coshell.update.UpdateError
import app.coshell.update.UpdatePhase
import app.coshell.update.UpdateUiState

/** The part of the update screen that changes with each step. */
@Composable
internal fun UpdatePhaseContent(state: UpdateUiState, actions: UpdateActions) {
    when (val phase = state.phase) {
        UpdatePhase.Checking -> {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            StatusText(stringResource(R.string.update_checking))
        }
        UpdatePhase.UpToDate -> {
            StatusText(stringResource(R.string.update_up_to_date))
            CoShellOutlinedButton(onClick = actions.onCheck) { Text(stringResource(R.string.update_check_again)) }
        }
        UpdatePhase.NotConfigured -> StatusText(stringResource(R.string.update_not_configured))
        is UpdatePhase.Available -> {
            ReleaseCard(phase.release)
            if (state.needsInstallPermission) {
                StatusText(stringResource(R.string.update_permission_hint))
                CoShellButton(onClick = actions.onAllowInstall) { Text(stringResource(R.string.update_allow_install)) }
            } else {
                CoShellButton(onClick = actions.onDownload) {
                    Text(stringResource(R.string.update_download, megabytes(phase.release.sizeBytes)))
                }
            }
        }
        is UpdatePhase.Downloading -> {
            ReleaseCard(phase.release)
            LinearProgressIndicator(progress = { phase.progress }, modifier = Modifier.fillMaxWidth())
            StatusText(stringResource(R.string.update_downloading, (phase.progress * 100).toInt()))
        }
        is UpdatePhase.Installing -> {
            ReleaseCard(phase.release)
            StatusText(stringResource(R.string.update_installing))
        }
        is UpdatePhase.Failed -> {
            phase.release?.let { ReleaseCard(it) }
            ErrorCard(stringResource(errorText(phase.error)))
            CoShellButton(onClick = if (phase.release != null) actions.onDownload else actions.onCheck) {
                Text(stringResource(R.string.update_retry))
            }
        }
    }
}

@Composable
private fun StatusText(text: String) = Text(text, style = MaterialTheme.typography.bodyLarge)

private fun errorText(error: UpdateError): Int = when (error) {
    UpdateError.Check -> R.string.update_error_check
    UpdateError.Download -> R.string.update_error_download
    UpdateError.Checksum -> R.string.update_error_checksum
    UpdateError.Install -> R.string.update_error_install
}

/** "4,2" for 4 400 000 bytes. */
internal fun megabytes(bytes: Long): String = "%.1f".format(bytes / 1_048_576.0)

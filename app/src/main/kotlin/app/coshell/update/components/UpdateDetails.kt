package app.coshell.update.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.update.UpdatePhase
import app.coshell.update.UpdateUiState

/**
 * Cards under the title (#268, boards Fb268A/B, Fb269A): which version is installed and which is coming,
 * what is in it, and on the install step a hint that «Установить вручную» is there right away.
 */
@Composable
internal fun UpdateDetails(state: UpdateUiState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        when (val phase = state.phase) {
            is UpdatePhase.UpToDate -> {
                InstalledVersionCard(state.installedVersionName, state.installedVersionCode, state.installedAt)
                phase.current?.notes?.takeIf { it.isNotBlank() }?.let {
                    ReleaseCard(stringResource(R.string.update_this_version_title), it)
                }
            }
            is UpdatePhase.Available -> {
                VersionCompareCard(state.installedVersionName, state.installedVersionCode, phase.release)
                WhatsNew(phase)
            }
            is UpdatePhase.Downloading -> WhatsNew(phase)
            is UpdatePhase.Installing -> {
                CoShellMessage(stringResource(R.string.update_installing_manual), isError = false)
                WhatsNew(phase)
            }
            else -> Unit
        }
    }
}

@Composable
private fun WhatsNew(phase: UpdatePhase) {
    val release = when (phase) {
        is UpdatePhase.Available -> phase.release
        is UpdatePhase.Downloading -> phase.release
        is UpdatePhase.Installing -> phase.release
        else -> null
    } ?: return
    if (release.notes.isNotBlank()) ReleaseCard(stringResource(R.string.update_whats_new_title, release.versionName), release.notes)
}

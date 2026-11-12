package app.coshell.update.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.update.UpdateActions
import app.coshell.update.UpdateError
import app.coshell.update.UpdatePhase
import app.coshell.update.UpdateUiState

/** Title, explanation and the one centred button under the ring; cross-fades between steps. */
@Composable
internal fun UpdatePhaseContent(state: UpdateUiState, actions: UpdateActions, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = state,
        contentKey = { it.phase.heroStep },
        transitionSpec = { fadeIn(Motion.effects()).togetherWith(fadeOut(Motion.effectsFast())) },
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxWidth(),
        label = "updateText",
    ) { shown ->
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            val installed = stringResource(R.string.update_installed, shown.installedVersionName, shown.installedVersionCode)
            when (val phase = shown.phase) {
                UpdatePhase.Checking -> Texts(stringResource(R.string.update_hero_checking), installed)
                UpdatePhase.UpToDate -> Texts(stringResource(R.string.update_hero_up_to_date), installed)
                UpdatePhase.NotConfigured ->
                    Texts(stringResource(R.string.update_hero_not_configured), stringResource(R.string.update_not_configured))
                is UpdatePhase.Available -> {
                    val release = phase.release
                    Texts(
                        stringResource(R.string.update_hero_available, release.versionName),
                        stringResource(R.string.update_hero_available_sub, release.versionCode, megabytes(release.sizeBytes)),
                    )
                    if (shown.needsInstallPermission) Subtitle(stringResource(R.string.update_permission_hint))
                }
                is UpdatePhase.Downloading -> Texts(
                    stringResource(R.string.update_hero_downloading),
                    stringResource(R.string.update_hero_available_sub, phase.release.versionCode, megabytes(phase.release.sizeBytes)),
                )
                is UpdatePhase.Installing -> Texts(
                    stringResource(R.string.update_hero_installing),
                    stringResource(if (phase.manualOffered) R.string.update_installing_manual else R.string.update_installing),
                )
                is UpdatePhase.Failed ->
                    Texts(stringResource(R.string.update_hero_failed), stringResource(errorText(phase.error)), isError = true)
            }
        }
    }
}

@Composable
private fun Texts(title: String, subtitle: String, isError: Boolean = false) {
    Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
    Subtitle(subtitle, isError)
}

@Composable
private fun Subtitle(text: String, isError: Boolean = false) = Text(
    text,
    style = MaterialTheme.typography.bodyMedium,
    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign = TextAlign.Center,
)

/**
 * The screen's single action, full width at the bottom (under the thumb): download / allow / retry are the
 * filled main button, «Проверить ещё раз» an outlined one; «Установить вручную» when the install dialog is
 * silent or Android failed (#217); nothing while checking or downloading.
 */
@Composable
internal fun UpdateMainAction(state: UpdateUiState, actions: UpdateActions, modifier: Modifier = Modifier) {
    val wide = modifier.fillMaxWidth()
    when (val phase = state.phase) {
        UpdatePhase.UpToDate ->
            CoShellOutlinedButton(onClick = actions.onCheck, modifier = wide) { Text(stringResource(R.string.update_check_again)) }
        is UpdatePhase.Available -> if (state.needsInstallPermission) {
            CoShellButton(onClick = actions.onAllowInstall, tactile = Tactile.Primary, modifier = wide) { Text(stringResource(R.string.update_allow_install)) }
        } else {
            CoShellButton(onClick = actions.onDownload, tactile = Tactile.Primary, modifier = wide) { Text(stringResource(R.string.update_open)) }
        }
        is UpdatePhase.Installing -> if (phase.manualOffered) {
            CoShellOutlinedButton(onClick = actions.onInstallManually, modifier = wide) {
                Text(stringResource(R.string.update_install_manually))
            }
        }
        is UpdatePhase.Failed -> if (phase.error == UpdateError.Install && phase.release != null) {
            CoShellButton(onClick = actions.onInstallManually, tactile = Tactile.Primary, modifier = wide) {
                Text(stringResource(R.string.update_install_manually))
            }
        } else {
            CoShellButton(
                onClick = if (phase.release != null) actions.onDownload else actions.onCheck,
                tactile = Tactile.Primary,
                modifier = wide,
            ) { Text(stringResource(R.string.update_retry)) }
        }
        else -> Unit
    }
}

private fun errorText(error: UpdateError): Int = when (error) {
    UpdateError.Check -> R.string.update_error_check
    UpdateError.Download -> R.string.update_error_download
    UpdateError.Checksum -> R.string.update_error_checksum
    UpdateError.Install -> R.string.update_error_install
}

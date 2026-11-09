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
                UpdatePhase.UpToDate -> {
                    Texts(stringResource(R.string.update_hero_up_to_date), installed)
                    CoShellOutlinedButton(onClick = actions.onCheck) { Text(stringResource(R.string.update_check_again)) }
                }
                UpdatePhase.NotConfigured ->
                    Texts(stringResource(R.string.update_hero_not_configured), stringResource(R.string.update_not_configured))
                is UpdatePhase.Available -> {
                    val release = phase.release
                    Texts(
                        stringResource(R.string.update_hero_available, release.versionName),
                        stringResource(R.string.update_hero_available_sub, release.versionCode, megabytes(release.sizeBytes)),
                    )
                    if (shown.needsInstallPermission) {
                        Subtitle(stringResource(R.string.update_permission_hint))
                        MainButton(stringResource(R.string.update_allow_install), actions.onAllowInstall)
                    } else {
                        MainButton(stringResource(R.string.update_open), actions.onDownload)
                    }
                }
                is UpdatePhase.Downloading -> Texts(
                    stringResource(R.string.update_hero_downloading),
                    stringResource(R.string.update_hero_available_sub, phase.release.versionCode, megabytes(phase.release.sizeBytes)),
                )
                is UpdatePhase.Installing ->
                    Texts(stringResource(R.string.update_hero_installing), stringResource(R.string.update_installing))
                is UpdatePhase.Failed -> {
                    Texts(stringResource(R.string.update_hero_failed), stringResource(errorText(phase.error)), isError = true)
                    MainButton(
                        stringResource(R.string.update_retry),
                        if (phase.release != null) actions.onDownload else actions.onCheck,
                    )
                }
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

/** The screen's single main action, centred under the ring. */
@Composable
private fun MainButton(text: String, onClick: () -> Unit) =
    CoShellButton(onClick = onClick, tactile = Tactile.Primary) { Text(text) }

private fun errorText(error: UpdateError): Int = when (error) {
    UpdateError.Check -> R.string.update_error_check
    UpdateError.Download -> R.string.update_error_download
    UpdateError.Checksum -> R.string.update_error_checksum
    UpdateError.Install -> R.string.update_error_install
}

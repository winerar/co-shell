package app.coshell.update.notice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellCheckMark
import app.coshell.core.designsystem.components.HeroRing
import app.coshell.core.designsystem.components.ProgressHero
import app.coshell.core.designsystem.motion.CoShellSuccessBurst
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.update.components.ReleaseNotesList
import app.coshell.core.designsystem.components.CoShellNoticePill
import app.coshell.core.domain.update.AppRelease
import app.coshell.core.designsystem.R as DesignR

/** Place once at the app root: keeps checking while the app is open and shows «Что нового» after an update. */
@Composable
fun UpdateNoticeEffects(viewModel: UpdateNoticeViewModel) {
    LifecycleStartEffect(viewModel) {
        viewModel.startWatching()
        onStopOrDispose { viewModel.stopWatching() }
    }
    val whatsNew by viewModel.whatsNew.collectAsStateWithLifecycle()
    whatsNew?.let { release ->
        UpdatedDialog(release, onDismiss = viewModel::dismissWhatsNew)
    }
}

/**
 * The quiet «new version» pill over the bottom bar. [release] null or [visible] false hides it.
 * A tap starts the download and [onOpen] goes to the update screen with its progress.
 */
@Composable
fun UpdatePill(
    release: AppRelease?,
    visible: Boolean,
    onOpen: (AppRelease) -> Unit,
    onDismiss: (AppRelease) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keeps the text while the pill slides away after the release turned null.
    var last by remember { mutableStateOf(release) }
    if (release != null) last = release
    val shown = last
    CoShellNoticePill(
        visible = visible && release != null,
        icon = DesignR.drawable.ic_download,
        text = shown?.let { stringResource(R.string.update_pill, it.versionName, it.versionCode) }.orEmpty(),
        onClick = { shown?.let(onOpen) },
        dismissLabel = stringResource(R.string.update_pill_dismiss),
        onDismiss = { shown?.let(onDismiss) },
        modifier = modifier,
    )
}

/** «Обновлено до 0.37» after an install: the ring with a check and the burst, then the labelled points and «Понятно». */
@Composable
private fun UpdatedDialog(release: AppRelease, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CoShellCard(Modifier.padding(horizontal = Spacing.l + Spacing.xs)) {
            Column(
                Modifier.fillMaxWidth().padding(Spacing.l),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.m),
            ) {
                CoShellSuccessBurst {
                    ProgressHero(ring = HeroRing.Idle, state = Unit) { CoShellCheckMark(Sizes.heroIcon) }
                }
                Text(stringResource(R.string.update_updated_title, release.versionName), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                if (release.notes.isNotBlank()) ReleaseNotesList(release.notes, Modifier.fillMaxWidth())
                CoShellButton(onClick = onDismiss, tactile = Tactile.Primary, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.update_whats_new_ok))
                }
            }
        }
    }
}

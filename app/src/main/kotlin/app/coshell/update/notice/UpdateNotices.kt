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
import app.coshell.core.designsystem.components.CoShellAlertDialog
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
        CoShellAlertDialog(
            title = stringResource(R.string.update_whats_new_title, release.versionName),
            text = release.notes,
            confirmLabel = stringResource(R.string.update_whats_new_ok),
            onConfirm = viewModel::dismissWhatsNew,
            onDismiss = viewModel::dismissWhatsNew,
        )
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
        text = shown?.let { stringResource(R.string.update_pill, it.versionCode) }.orEmpty(),
        onClick = { shown?.let(onOpen) },
        dismissLabel = stringResource(R.string.update_pill_dismiss),
        onDismiss = { shown?.let(onDismiss) },
        modifier = modifier,
    )
}

package app.coshell.crash.notice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAlertDialog

/** Place once at the app root: after a crash asks whether to send it to the developers (#218). */
@Composable
fun CrashNotice(viewModel: CrashNoticeViewModel) {
    val crash by viewModel.crash.collectAsStateWithLifecycle()
    if (crash != null) {
        CoShellAlertDialog(
            title = stringResource(R.string.crash_notice_title),
            text = stringResource(R.string.crash_notice_text),
            confirmLabel = stringResource(R.string.crash_notice_send),
            onConfirm = viewModel::send,
            onDismiss = viewModel::dismiss,
            dismissLabel = stringResource(R.string.crash_notice_skip),
        )
    }
}

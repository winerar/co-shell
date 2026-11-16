package app.coshell.feedback.components

import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.components.CoShellStatusChip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.domain.feedback.FeedbackStage
import app.coshell.core.domain.feedback.FeedbackStatus

/** Status label of a sent message (board `CompNew` «Точка и статус»): done — green, in work — amber, waiting or postponed — grey. */
@Composable
internal fun FeedbackStatusChip(status: FeedbackStatus, modifier: Modifier = Modifier) {
    val tone = when (status.stage) {
        FeedbackStage.DONE -> StatusTone.Done
        FeedbackStage.IN_PROGRESS -> StatusTone.Work
        FeedbackStage.SENT, FeedbackStage.RECEIVED, FeedbackStage.LATER, FeedbackStage.CLOSED -> StatusTone.No
    }
    CoShellStatusChip(feedbackStatusLabel(status), tone, modifier)
}

@Composable
internal fun feedbackStatusLabel(status: FeedbackStatus): String = when (status.stage) {
    FeedbackStage.SENT -> stringResource(R.string.feedback_status_sent)
    FeedbackStage.RECEIVED -> stringResource(R.string.feedback_status_received)
    FeedbackStage.IN_PROGRESS -> stringResource(R.string.feedback_status_in_progress)
    FeedbackStage.DONE -> status.doneBuild?.let { stringResource(R.string.feedback_status_done_build, it) }
        ?: stringResource(R.string.feedback_status_done)
    FeedbackStage.LATER -> stringResource(R.string.feedback_status_later)
    FeedbackStage.CLOSED -> stringResource(R.string.feedback_status_closed)
}

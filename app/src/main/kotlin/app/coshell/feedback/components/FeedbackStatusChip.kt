package app.coshell.feedback.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.feedback.FeedbackStage
import app.coshell.core.domain.feedback.FeedbackStatus

/** Status pill of a sent message (board Fb157A): lime — done, violet — in work, grey — waiting or postponed. */
@Composable
internal fun FeedbackStatusChip(status: FeedbackStatus, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = when (status.stage) {
        FeedbackStage.DONE -> scheme.primary to scheme.onPrimary
        FeedbackStage.IN_PROGRESS -> scheme.secondaryContainer to scheme.onSecondaryContainer
        FeedbackStage.SENT, FeedbackStage.RECEIVED -> scheme.surfaceContainerHighest to scheme.onSurface
        FeedbackStage.LATER, FeedbackStage.CLOSED -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
    }
    Text(
        feedbackStatusLabel(status),
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        maxLines = 1,
        modifier = modifier
            .background(bg, PillShape)
            .heightIn(min = Sizes.statusChipHeight)
            .wrapContentHeight()
            .padding(horizontal = Spacing.s, vertical = Spacing.xxs),
    )
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

package app.coshell.feedback.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.domain.feedback.FeedbackResult
import app.coshell.core.designsystem.components.CoShellMessage

/** Outcome of the last send. */
@Composable
internal fun FeedbackResultCard(result: FeedbackResult) {
    val text = when (result) {
        is FeedbackResult.Sent -> stringResource(R.string.feedback_result_sent)
        FeedbackResult.Empty -> stringResource(R.string.feedback_error_empty)
        is FeedbackResult.TooLong -> stringResource(R.string.feedback_error_too_long, result.maxLength)
        FeedbackResult.NotConfigured -> stringResource(R.string.feedback_error_not_configured)
        is FeedbackResult.Failed -> stringResource(R.string.feedback_error_failed)
    }
    CoShellMessage(text, isError = result !is FeedbackResult.Sent)
}

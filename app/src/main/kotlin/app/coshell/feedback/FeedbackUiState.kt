package app.coshell.feedback

import app.coshell.core.domain.feedback.FeedbackResult
import app.coshell.feedback.data.SentFeedback

/** State of the feedback form; [result] of the last send is shown until the text changes. */
data class FeedbackUiState(
    val text: String = "",
    val kind: FeedbackKind = FeedbackKind.BUG,
    val isSending: Boolean = false,
    val result: FeedbackResult? = null,
    /** Messages sent from this phone, newest first. */
    val history: List<SentFeedback> = emptyList(),
) {
    val canSend get() = text.isNotBlank() && !isSending
    val justSent get() = result is FeedbackResult.Sent
}

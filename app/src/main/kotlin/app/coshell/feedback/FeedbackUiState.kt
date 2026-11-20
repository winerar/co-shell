package app.coshell.feedback

import app.coshell.core.domain.feedback.FeedbackAttachment
import app.coshell.core.domain.feedback.FeedbackResult
import app.coshell.feedback.data.SentFeedback
import app.coshell.feedback.files.FeedbackFile

/** State of the feedback form; [result] of the last send is shown until the text changes. */
data class FeedbackUiState(
    val text: String = "",
    val kind: FeedbackKind = FeedbackKind.BUG,
    val isSending: Boolean = false,
    val result: FeedbackResult? = null,
    /** Messages sent from this phone, newest first. */
    val history: List<SentFeedback> = emptyList(),
    /** Attached files (#561), in the order chosen; at most [FeedbackAttachment.MAX_COUNT]. */
    val files: List<FeedbackFile> = emptyList(),
    /** The picked file could not be read (or is over 10 MB). */
    val fileError: Boolean = false,
) {
    val canAttach get() = files.size < FeedbackAttachment.MAX_COUNT && !isSending

    val canSend get() = text.isNotBlank() && !isSending
    val justSent get() = result is FeedbackResult.Sent
}

package app.coshell.feedback

import app.coshell.core.domain.feedback.FeedbackResult

/** State of the feedback form; [result] of the last send is shown until the text changes. */
data class FeedbackUiState(
    val text: String = "",
    val isSending: Boolean = false,
    val result: FeedbackResult? = null,
) {
    val canSend get() = text.isNotBlank() && !isSending
}

package app.coshell.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.feedback.FeedbackResult
import app.coshell.core.domain.feedback.SendFeedbackUseCase
import app.coshell.feedback.data.FeedbackHistory
import app.coshell.feedback.data.InMemoryFeedbackHistory
import app.coshell.feedback.data.SentFeedback
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Sends the typed text with its kind in front; on success clears the field and remembers it, on failure keeps it. */
@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val sendFeedback: SendFeedbackUseCase,
    private val history: FeedbackHistory = InMemoryFeedbackHistory(),
    private val clock: Clock = Clock.System,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedbackUiState())
    val state: StateFlow<FeedbackUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { history.entries.collect { list -> _state.update { it.copy(history = list) } } }
    }

    fun onTextChange(text: String) = _state.update { it.copy(text = text, result = null) }

    /** Starts the field with [text] (an error code from the sync banner) unless something is typed already. */
    fun prefill(text: String) = _state.update { if (it.text.isBlank()) it.copy(text = text) else it }

    fun onKind(kind: FeedbackKind) = _state.update { it.copy(kind = kind) }

    /** «Написать ещё» after a successful send. */
    fun onWriteMore() = _state.update { it.copy(result = null) }

    /** [kindLabel] — the localized name of [FeedbackUiState.kind]; `null` sends the text alone. */
    fun send(kindLabel: String? = null) {
        if (!_state.value.canSend) return
        val message = kindLabel?.let { feedbackMessage(it, _state.value.text) } ?: _state.value.text
        _state.update { it.copy(isSending = true, result = null) }
        viewModelScope.launch {
            val result = sendFeedback(message)
            if (result == FeedbackResult.Sent) history.add(SentFeedback(clock.now().toEpochMilliseconds(), message.trim()))
            _state.update {
                it.copy(
                    text = if (result == FeedbackResult.Sent) "" else it.text,
                    isSending = false,
                    result = result,
                )
            }
        }
    }
}

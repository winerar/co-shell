package app.coshell.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.feedback.FeedbackResult
import app.coshell.core.domain.feedback.SendFeedbackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Sends the typed text; on success clears the field, on failure keeps it for another try. */
@HiltViewModel
class FeedbackViewModel @Inject constructor(private val sendFeedback: SendFeedbackUseCase) : ViewModel() {

    private val _state = MutableStateFlow(FeedbackUiState())
    val state: StateFlow<FeedbackUiState> = _state.asStateFlow()

    fun onTextChange(text: String) = _state.update { it.copy(text = text, result = null) }

    fun send() {
        if (!_state.value.canSend) return
        _state.update { it.copy(isSending = true, result = null) }
        viewModelScope.launch {
            val result = sendFeedback(_state.value.text)
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

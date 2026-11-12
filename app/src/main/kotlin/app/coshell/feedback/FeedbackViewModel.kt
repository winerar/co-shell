package app.coshell.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.feedback.FeedbackResult
import app.coshell.core.domain.feedback.FeedbackStage
import app.coshell.core.domain.feedback.FeedbackStatus
import app.coshell.core.domain.feedback.FeedbackStatusSource
import app.coshell.core.domain.feedback.SendFeedbackUseCase
import app.coshell.core.domain.feedback.isDoneAndSeen
import app.coshell.feedback.data.FeedbackHistory
import app.coshell.feedback.data.InMemoryFeedbackHistory
import app.coshell.feedback.data.SentFeedback
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sends the typed text with its kind in front; on success clears the field and remembers it, on failure keeps it.
 * «Мои отзывы»: on open asks once for the status of every message that may still change (#157), then drops
 * the ones done in an installed build two weeks ago (#174; only from this phone, the task stays).
 */
@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val sendFeedback: SendFeedbackUseCase,
    private val history: FeedbackHistory = InMemoryFeedbackHistory(),
    private val clock: Clock = Clock.System,
    private val statuses: FeedbackStatusSource = NoFeedbackStatus,
    private val installed: InstalledBuild = InstalledBuild(0),
) : ViewModel() {

    private val _state = MutableStateFlow(FeedbackUiState())
    val state: StateFlow<FeedbackUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { history.entries.collect { list -> _state.update { it.copy(history = list) } } }
        viewModelScope.launch {
            refreshStatuses()
            dropDoneAndSeen()
        }
    }

    private suspend fun dropDoneAndSeen() {
        val now = clock.now()
        history.entries.first()
            .filter { it.status?.isDoneAndSeen(now, installed.versionCode) == true }
            .forEach { history.remove(it.sentAtMillis) }
    }

    /** One read per message that is not settled yet; a failed read keeps the last known status. */
    private suspend fun refreshStatuses() {
        history.entries.first()
            .filter { it.documentId.isNotEmpty() && it.status?.isSettled != true }
            .forEach { entry ->
                val status = statuses.status(entry.documentId) ?: return@forEach
                if (status != entry.status) history.setStatus(entry.sentAtMillis, status)
            }
    }

    /** Long press in the list or «Убрать из списка» on the card: the task itself stays. */
    fun remove(sentAtMillis: Long) {
        viewModelScope.launch { history.remove(sentAtMillis) }
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
            if (result is FeedbackResult.Sent) {
                val sentAt = clock.now().toEpochMilliseconds()
                history.add(SentFeedback(sentAt, message.trim(), result.documentId, FeedbackStatus(FeedbackStage.SENT)))
            }
            _state.update {
                it.copy(
                    text = if (result is FeedbackResult.Sent) "" else it.text,
                    isSending = false,
                    result = result,
                )
            }
        }
    }
}

/** The `versionCode` of the app on this phone (`BuildConfig.VERSION_CODE`, given by `FeedbackModule`). */
data class InstalledBuild(val versionCode: Int)

/** No status anywhere (previews, tests, builds without Firebase). */
object NoFeedbackStatus : FeedbackStatusSource {
    override suspend fun status(documentId: String): FeedbackStatus? = null
}

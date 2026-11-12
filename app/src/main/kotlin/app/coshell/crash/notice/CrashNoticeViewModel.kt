package app.coshell.crash.notice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.feedback.FeedbackResult
import app.coshell.core.domain.feedback.SendFeedbackUseCase
import app.coshell.crash.CrashLog
import app.coshell.crash.crashTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * After a crash (#218): asks once per launch whether to send it. The message goes through «Обратная связь»
 * (Firestore → issue); the stack trace travels in its technical data (`AndroidDiagnosticsProvider`).
 */
@HiltViewModel
class CrashNoticeViewModel @Inject constructor(
    private val log: CrashLog,
    private val sendFeedback: SendFeedbackUseCase,
) : ViewModel() {

    /** Short name of the crash while the question is on screen. */
    private val _crash = MutableStateFlow(log.pending()?.let(::crashTitle))
    val crash: StateFlow<String?> = _crash.asStateFlow()

    fun send() {
        val title = _crash.value ?: return
        _crash.value = null
        viewModelScope.launch {
            // Not sent (offline, local build) — the crash stays and the question comes back next launch.
            if (sendFeedback("$PREFIX $title") is FeedbackResult.Sent) log.clear()
        }
    }

    fun dismiss() {
        _crash.value = null
        log.clear()
    }

    companion object {
        /** Start of the message: the issue title reads «Отзыв: Сбой: …». */
        const val PREFIX = "Сбой:"
    }
}

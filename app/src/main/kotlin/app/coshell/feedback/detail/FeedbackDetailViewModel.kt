package app.coshell.feedback.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.feedback.data.FeedbackHistory
import app.coshell.feedback.data.SentFeedback
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** The card of one sent message; its status is refreshed by the list screen it is opened from. */
@HiltViewModel
class FeedbackDetailViewModel @Inject constructor(private val history: FeedbackHistory) : ViewModel() {

    /** `null` once the message is removed. */
    fun entry(sentAtMillis: Long): Flow<SentFeedback?> =
        history.entries.map { list -> list.firstOrNull { it.sentAtMillis == sentAtMillis } }

    fun remove(sentAtMillis: Long) {
        viewModelScope.launch { history.remove(sentAtMillis) }
    }
}

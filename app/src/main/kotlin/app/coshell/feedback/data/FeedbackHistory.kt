package app.coshell.feedback.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.feedback.FeedbackStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * One sent message: when, what (with its kind in front, as sent), its Firestore [documentId]
 * (empty for messages sent before #157) and the last known [status]. [sentAtMillis] is the key.
 */
data class SentFeedback(
    val sentAtMillis: Long,
    val text: String,
    val documentId: String = "",
    val status: FeedbackStatus? = null,
)

/** «Мои отзывы»: messages sent from this phone, newest first. */
interface FeedbackHistory {
    val entries: Flow<List<SentFeedback>>
    suspend fun add(entry: SentFeedback)
    suspend fun setStatus(sentAtMillis: Long, status: FeedbackStatus)
    /** «Убрать из списка»: only from this phone, the task stays. */
    suspend fun remove(sentAtMillis: Long)
}

/** Shared list logic; [load]/[save] — where the list lives. Keeps [MAX] newest. */
abstract class ListFeedbackHistory : FeedbackHistory {
    private val state = MutableStateFlow<List<SentFeedback>?>(null)
    private val lock = Mutex()

    protected abstract suspend fun load(): List<SentFeedback>
    protected abstract suspend fun save(list: List<SentFeedback>)

    override val entries: Flow<List<SentFeedback>> = state
        .onStart { if (state.value == null) state.compareAndSet(null, load()) }
        .filterNotNull()

    override suspend fun add(entry: SentFeedback) = change { (listOf(entry) + it).take(MAX) }

    override suspend fun setStatus(sentAtMillis: Long, status: FeedbackStatus) =
        change { list -> list.map { if (it.sentAtMillis == sentAtMillis) it.copy(status = status) else it } }

    override suspend fun remove(sentAtMillis: Long) = change { list -> list.filterNot { it.sentAtMillis == sentAtMillis } }

    private suspend fun change(transform: (List<SentFeedback>) -> List<SentFeedback>) = lock.withLock {
        val next = transform(state.value ?: load())
        state.value = next
        save(next)
    }

    private companion object {
        const val MAX = 20
    }
}

/** [FeedbackHistory] in the device "settings" file as JSON ([encodeFeedbackHistory]). */
@Singleton
class PrefsFeedbackHistory @Inject constructor(@ApplicationContext context: Context) : ListFeedbackHistory() {
    private val prefs by lazy { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    override suspend fun load() = withContext(Dispatchers.IO) { decodeFeedbackHistory(prefs.getString(KEY, null).orEmpty()) }

    override suspend fun save(list: List<SentFeedback>) = withContext(Dispatchers.IO) {
        prefs.edit { putString(KEY, encodeFeedbackHistory(list)) }
    }

    private companion object {
        const val KEY = "feedback_history"
    }
}

/** History kept only while the app runs — previews and tests. */
class InMemoryFeedbackHistory(initial: List<SentFeedback> = emptyList()) : ListFeedbackHistory() {
    private var stored = initial
    override suspend fun load() = stored
    override suspend fun save(list: List<SentFeedback>) {
        stored = list
    }
}

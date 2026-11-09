package app.coshell.feedback.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/** One sent message: when and what (with its kind in front, as sent). */
data class SentFeedback(val sentAtMillis: Long, val text: String)

/** The last messages sent from this phone, newest first — shown under the form («Отправленные»). */
interface FeedbackHistory {
    val entries: Flow<List<SentFeedback>>
    suspend fun add(entry: SentFeedback)
}

/** [FeedbackHistory] in the device "settings" file, one line per message; keeps [MAX] newest. */
@Singleton
class PrefsFeedbackHistory @Inject constructor(@ApplicationContext context: Context) : FeedbackHistory {
    private val prefs by lazy { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    private val state = MutableStateFlow<List<SentFeedback>?>(null)

    override val entries: Flow<List<SentFeedback>> = state
        .onStart { if (state.value == null) state.compareAndSet(null, io { decodeFeedbackHistory(prefs.getString(KEY, null).orEmpty()) }) }
        .filterNotNull()

    override suspend fun add(entry: SentFeedback) {
        val current = state.value ?: io { decodeFeedbackHistory(prefs.getString(KEY, null).orEmpty()) }
        val next = (listOf(entry) + current).take(MAX)
        state.value = next
        io { prefs.edit { putString(KEY, encodeFeedbackHistory(next)) } }
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private companion object {
        const val KEY = "feedback_history"
        const val MAX = 10
    }
}

/** `millis<TAB>text` per line; line breaks inside a text are kept as `\n`. */
internal fun encodeFeedbackHistory(entries: List<SentFeedback>): String =
    entries.joinToString("\n") { "${it.sentAtMillis}\t" + it.text.replace("\\", "\\\\").replace("\n", "\\n") }

internal fun decodeFeedbackHistory(raw: String): List<SentFeedback> = raw.lines().mapNotNull { line ->
    val millis = line.substringBefore('\t', "").toLongOrNull() ?: return@mapNotNull null
    val text = line.substringAfter('\t').replace(Regex("""\\(\\|n)""")) { if (it.groupValues[1] == "n") "\n" else "\\" }
    SentFeedback(millis, text)
}

/** History kept only while the app runs — previews and tests. */
class InMemoryFeedbackHistory : FeedbackHistory {
    private val state = MutableStateFlow(emptyList<SentFeedback>())
    override val entries: Flow<List<SentFeedback>> = state
    override suspend fun add(entry: SentFeedback) {
        state.value = listOf(entry) + state.value
    }
}

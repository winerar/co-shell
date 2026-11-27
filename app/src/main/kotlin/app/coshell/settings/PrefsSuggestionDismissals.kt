package app.coshell.settings

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.settings.SuggestionDismissals
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/** [SuggestionDismissals] in its own small SharedPreferences file: turned-down suggestions stay on this phone (#791, #789), like hints. */
@Singleton
class PrefsSuggestionDismissals @Inject constructor(@ApplicationContext context: Context) : SuggestionDismissals {

    private val prefs by lazy { context.getSharedPreferences(FILE, Context.MODE_PRIVATE) }

    /** In-memory copy; `null` until the file is read once off the main thread. */
    private val state = MutableStateFlow<Set<String>?>(null)

    override val dismissed: Flow<Set<String>> = state
        .onStart { if (state.value == null) state.compareAndSet(null, io { read() }) }
        .filterNotNull()

    override suspend fun dismiss(key: String) {
        val next = (state.value ?: io { read() }) + key
        state.value = next
        io { prefs.edit { putStringSet(KEY, next) } }
    }

    private fun read(): Set<String> = prefs.getStringSet(KEY, emptySet()).orEmpty().toSet()

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private companion object {
        const val FILE = "suggestions"
        const val KEY = "dismissed"
    }
}

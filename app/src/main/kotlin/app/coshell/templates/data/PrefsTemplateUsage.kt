package app.coshell.templates.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.templates.TemplateUsage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/** [TemplateUsage] in its own small SharedPreferences file: one int per template id. */
@Singleton
class PrefsTemplateUsage @Inject constructor(@ApplicationContext context: Context) : TemplateUsage {

    private val prefs by lazy { context.getSharedPreferences("template_usage", Context.MODE_PRIVATE) }

    /** In-memory copy; `null` until the file is read once off the main thread. */
    private val state = MutableStateFlow<Map<String, Int>?>(null)

    override val counts: Flow<Map<String, Int>> = state
        .onStart { load() }
        .filterNotNull()

    override suspend fun record(templateId: String) {
        val next = load().let { it + (templateId to (it[templateId] ?: 0) + 1) }
        state.value = next
        io { prefs.edit { putInt(templateId, next.getValue(templateId)) } }
    }

    private suspend fun load(): Map<String, Int> {
        state.value?.let { return it }
        val read = io { prefs.all.mapNotNull { (id, count) -> (count as? Int)?.let { id to it } }.toMap() }
        state.compareAndSet(null, read)
        return state.value ?: read
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }
}

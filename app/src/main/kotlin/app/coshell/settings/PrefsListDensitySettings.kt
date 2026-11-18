package app.coshell.settings

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.settings.ListDensitySettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/** [ListDensitySettings] in the small «settings» SharedPreferences file (a device preference, not part of the backup). */
@Singleton
class PrefsListDensitySettings @Inject constructor(@ApplicationContext context: Context) : ListDensitySettings {

    private val prefs by lazy { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    /** In-memory copy; `null` until the file is read once off the main thread. */
    private val state = MutableStateFlow<Boolean?>(null)

    override val compact: Flow<Boolean> = state
        .onStart { if (state.value == null) state.compareAndSet(null, io { prefs.getBoolean(KEY, false) }) }
        .filterNotNull()

    override suspend fun setCompact(compact: Boolean) {
        state.value = compact
        io { prefs.edit { putBoolean(KEY, compact) } }
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private companion object {
        const val KEY = "list_compact"
    }
}

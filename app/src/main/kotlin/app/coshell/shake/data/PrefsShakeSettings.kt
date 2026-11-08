package app.coshell.shake.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.shake.ShakeSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/** [ShakeSettings] in a small SharedPreferences file (a device preference, not part of the backup). */
@Singleton
class PrefsShakeSettings @Inject constructor(@ApplicationContext context: Context) : ShakeSettings {

    private val prefs by lazy { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    /** In-memory copy; `null` until the file is read once off the main thread. */
    private val state = MutableStateFlow<Boolean?>(null)

    override val enabled: Flow<Boolean> = state
        .onStart { if (state.value == null) state.compareAndSet(null, io { prefs.getBoolean(KEY_ENABLED, true) }) }
        .filterNotNull()

    override suspend fun setEnabled(enabled: Boolean) {
        state.value = enabled
        io { prefs.edit { putBoolean(KEY_ENABLED, enabled) } }
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private companion object {
        const val KEY_ENABLED = "shake_to_add"
    }
}

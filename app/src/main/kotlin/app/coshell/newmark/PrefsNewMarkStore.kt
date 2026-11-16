package app.coshell.newmark

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.newmark.NewMarkStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** [NewMarkStore] in a small SharedPreferences file: per phone, not synced and not in the backup. */
@Singleton
class PrefsNewMarkStore @Inject constructor(@ApplicationContext context: Context) : NewMarkStore {

    private val prefs by lazy { context.getSharedPreferences("new_marks", Context.MODE_PRIVATE) }

    override suspend fun isStarted(): Boolean = io { prefs.getBoolean(KEY_STARTED, false) }

    override suspend fun setStarted() = io { prefs.edit { putBoolean(KEY_STARTED, true) } }

    override suspend fun shownSince(key: String): Instant? = io {
        prefs.getLong(SHOWN + key, 0L).takeIf { it > 0 }?.let { Instant.fromEpochMilliseconds(it) }
    }

    override suspend fun setShownSince(key: String, at: Instant) = io {
        prefs.edit { putLong(SHOWN + key, at.toEpochMilliseconds()) }
    }

    override suspend fun isOpened(key: String): Boolean = io { prefs.getBoolean(OPENED + key, false) }

    override suspend fun setOpened(key: String) = io { prefs.edit { putBoolean(OPENED + key, true) } }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private companion object {
        const val KEY_STARTED = "started"
        const val SHOWN = "shown_since:"
        const val OPENED = "opened:"
    }
}

package app.coshell.update.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.update.AppRelease
import app.coshell.core.domain.update.UpdateStateStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** [UpdateStateStore] in a small SharedPreferences file (not part of the backup). */
@Singleton
class PrefsUpdateStateStore @Inject constructor(@ApplicationContext context: Context) : UpdateStateStore {

    private val prefs by lazy { context.getSharedPreferences("updates", Context.MODE_PRIVATE) }

    override suspend fun lastCheckAt(): Instant? = io {
        prefs.getLong(KEY_LAST_CHECK, 0L).takeIf { it > 0 }?.let { Instant.fromEpochMilliseconds(it) }
    }

    override suspend fun setLastCheckAt(at: Instant) = io {
        prefs.edit { putLong(KEY_LAST_CHECK, at.toEpochMilliseconds()) }
    }

    override suspend fun pendingRelease(): AppRelease? = io {
        prefs.getString(KEY_PENDING, null)?.let(::releaseFromJson)
    }

    override suspend fun setPendingRelease(release: AppRelease?) = io {
        prefs.edit { if (release == null) remove(KEY_PENDING) else putString(KEY_PENDING, release.toJson()) }
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private companion object {
        const val KEY_LAST_CHECK = "last_check_at"
        const val KEY_PENDING = "pending_release"
    }
}

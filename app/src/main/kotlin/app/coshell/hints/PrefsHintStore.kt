package app.coshell.hints

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.hints.HintStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** [HintStore] in a small SharedPreferences file: per phone, not synced and not in the backup (like `ThemeSettings`). */
@Singleton
class PrefsHintStore @Inject constructor(@ApplicationContext context: Context) : HintStore {

    private val prefs by lazy { context.getSharedPreferences("hints", Context.MODE_PRIVATE) }

    override suspend fun enabled(): Boolean = io { prefs.getBoolean(ENABLED, true) }

    override suspend fun setEnabled(value: Boolean) = io { prefs.edit { putBoolean(ENABLED, value) } }

    override suspend fun shown(): Set<String> = io { prefs.getStringSet(SHOWN, emptySet()).orEmpty().toSet() }

    override suspend fun markShown(key: String) = io { prefs.edit { putStringSet(SHOWN, prefs.getStringSet(SHOWN, emptySet()).orEmpty() + key) } }

    override suspend fun lastShownDay(): Long? = io { prefs.getLong(LAST_DAY, NONE).takeIf { it != NONE } }

    override suspend fun setLastShownDay(day: Long) = io { prefs.edit { putLong(LAST_DAY, day) } }

    override suspend fun stepsHidden(): Boolean = io { prefs.getBoolean(STEPS_HIDDEN, false) }

    override suspend fun setStepsHidden(value: Boolean) = io { prefs.edit { putBoolean(STEPS_HIDDEN, value) } }

    override suspend fun reset() = io { prefs.edit { clear() } }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private companion object {
        const val ENABLED = "enabled"
        const val SHOWN = "shown"
        const val LAST_DAY = "last_day"
        const val STEPS_HIDDEN = "steps_hidden"
        const val NONE = Long.MIN_VALUE
    }
}

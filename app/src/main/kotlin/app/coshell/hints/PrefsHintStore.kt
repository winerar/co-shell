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

    override suspend fun firstSeenDay(today: Long): Long = io {
        prefs.getLong(FIRST_SEEN, NONE).takeIf { it != NONE } ?: today.also { prefs.edit { putLong(FIRST_SEEN, it) } }
    }

    override suspend fun firstEntryDay(candidate: Long?): Long? = io {
        prefs.getLong(FIRST_ENTRY, NONE).takeIf { it != NONE }
            ?: candidate?.also { prefs.edit { putLong(FIRST_ENTRY, it) } }
    }

    override suspend fun tourChecked(): Boolean = io { prefs.getBoolean(TOUR_CHECKED, false) }

    override suspend fun setTourChecked() = io { prefs.edit { putBoolean(TOUR_CHECKED, true) } }

    // Keeps the dates (and the tour check): «show hints again» forgets what was shown, not when the phone started.
    override suspend fun reset() = io {
        prefs.edit {
            remove(SHOWN)
            remove(LAST_DAY)
            remove(STEPS_HIDDEN)
            putBoolean(ENABLED, true)
        }
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private companion object {
        const val ENABLED = "enabled"
        const val SHOWN = "shown"
        const val LAST_DAY = "last_day"
        const val FIRST_SEEN = "first_seen"
        const val FIRST_ENTRY = "first_entry"
        const val STEPS_HIDDEN = "steps_hidden"
        const val TOUR_CHECKED = "tour_checked"
        const val NONE = Long.MIN_VALUE
    }
}

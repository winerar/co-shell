package app.coshell.recurring.notify.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.settings.ReminderSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/** [ReminderSettings] in the device's `settings` file, like the shake switch. */
@Singleton
class PrefsReminderSettings @Inject constructor(@ApplicationContext context: Context) : ReminderSettings {

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

    private val hourState = MutableStateFlow<Int?>(null)

    override val hour: Flow<Int> = hourState
        .onStart { if (hourState.value == null) hourState.compareAndSet(null, io { storedHour(prefs) }) }
        .filterNotNull()

    override suspend fun setHour(hour: Int) {
        hourState.value = hour
        io { prefs.edit { putInt(KEY_HOUR, hour) } }
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    companion object {
        private const val KEY_ENABLED = "payment_reminders"
        private const val KEY_HOUR = "reminder_hour"

        private fun storedHour(prefs: android.content.SharedPreferences): Int =
            prefs.getInt(KEY_HOUR, ReminderSettings.DEFAULT_HOUR).takeIf { it in 0..23 } ?: ReminderSettings.DEFAULT_HOUR

        /** The hour for the scheduler, which starts without Hilt. */
        fun readHour(context: Context): Int = storedHour(context.getSharedPreferences("settings", Context.MODE_PRIVATE))
    }
}

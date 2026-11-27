package app.coshell.recurring.notify.data

import android.content.Context
import androidx.core.content.edit
import app.coshell.core.domain.settings.ClockMinutes
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

    private val timeState = MutableStateFlow<Int?>(null)

    override val time: Flow<Int> = timeState
        .onStart { if (timeState.value == null) timeState.compareAndSet(null, io { storedTime(prefs) }) }
        .filterNotNull()

    override suspend fun setTime(minutes: Int) {
        timeState.value = minutes
        io { prefs.edit { putInt(KEY_TIME, minutes) } }
    }

    private val daysState = MutableStateFlow<Int?>(null)

    override val daysBefore: Flow<Int> = daysState
        .onStart { if (daysState.value == null) daysState.compareAndSet(null, io { prefs.getInt(KEY_DAYS, 0).takeIf { it in 0..ReminderSettings.MAX_DAYS_BEFORE } ?: 0 }) }
        .filterNotNull()

    override suspend fun setDaysBefore(days: Int) {
        daysState.value = days
        io { prefs.edit { putInt(KEY_DAYS, days) } }
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    companion object {
        private const val KEY_ENABLED = "payment_reminders"
        /** Before #777 only whole hours: read once as a fallback for [KEY_TIME]. */
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_TIME = "reminder_time"
        private const val KEY_DAYS = "reminder_days_before"

        private fun storedTime(prefs: android.content.SharedPreferences): Int =
            prefs.getInt(KEY_TIME, -1).takeIf { ClockMinutes.isValid(it) }
                ?: prefs.getInt(KEY_HOUR, -1).takeIf { it in 0..23 }?.let { ClockMinutes.of(it) }
                ?: ReminderSettings.DEFAULT_TIME

        /** The time for the scheduler, which starts without Hilt. */
        fun readTime(context: Context): Int = storedTime(context.getSharedPreferences("settings", Context.MODE_PRIVATE))
    }
}

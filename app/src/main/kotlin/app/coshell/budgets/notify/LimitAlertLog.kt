package app.coshell.budgets.notify

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.LocalDate

/**
 * What the limit notification (#186) already said: the day of the last check (one a day) and the keys
 * `LimitAlert.key` sent this month (each limit hears of each kind once a month). Phone-only, like `ReminderLog`.
 */
@Singleton
class LimitAlertLog @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("limit_alerts", Context.MODE_PRIVATE)

    fun checkedOn(day: LocalDate): Boolean = prefs.getString(LAST, null) == day.toString()

    fun told(): Set<String> = prefs.getStringSet(TOLD, null).orEmpty()

    /** The check of [day] is done; [key] — what was sent, if anything. Keys of other months are dropped. */
    fun markChecked(day: LocalDate, key: String?, month: String) {
        val keep = told().filter { it.split(':').getOrNull(1) == month } + listOfNotNull(key)
        prefs.edit().putString(LAST, day.toString()).putStringSet(TOLD, keep.toSet()).apply()
    }

    private companion object {
        const val LAST = "last_checked"
        const val TOLD = "told"
    }
}

package app.coshell.recurring.notify

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.LocalDate

/** The day the 9:00 reminder last went out: a second run on the same day stays quiet. Phone-only. */
@Singleton
class ReminderLog @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("recurring_reminders", Context.MODE_PRIVATE)

    fun remindedOn(day: LocalDate): Boolean = prefs.getString(LAST, null) == day.toString()

    fun markReminded(day: LocalDate) = prefs.edit().putString(LAST, day.toString()).apply()

    private companion object {
        const val LAST = "last_reminded"
    }
}

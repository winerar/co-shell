package app.coshell.recurring.notify

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.LocalDate

/**
 * The day the 9:00 reminder last went out: a second run on the same day stays quiet. And «Завтра» (#405): the
 * templates whose reminder was put off till a day. Phone-only.
 */
@Singleton
class ReminderLog @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("recurring_reminders", Context.MODE_PRIVATE)

    fun remindedOn(day: LocalDate): Boolean = prefs.getString(LAST, null) == day.toString()

    fun markReminded(day: LocalDate) = prefs.edit().putString(LAST, day.toString()).apply()

    /** «Завтра» on the reminder: [templateIds] are asked again on [day]. */
    fun snooze(templateIds: Collection<String>, day: LocalDate) =
        prefs.edit().putStringSet(SNOOZED_IDS, templateIds.toSet()).putString(SNOOZED_DAY, day.toString()).apply()

    /** Templates put off till [day]; an older «Завтра» has done its job. */
    fun snoozedFor(day: LocalDate): Set<String> =
        if (prefs.getString(SNOOZED_DAY, null) == day.toString()) prefs.getStringSet(SNOOZED_IDS, null).orEmpty() else emptySet()

    private companion object {
        const val LAST = "last_reminded"
        const val SNOOZED_IDS = "snoozed_ids"
        const val SNOOZED_DAY = "snoozed_day"
    }
}

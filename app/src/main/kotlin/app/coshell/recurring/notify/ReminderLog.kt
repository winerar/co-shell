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

    /** The reminder of [day] went out about [templateIds] — [ReminderWatch] takes it back once they are handled. */
    fun markReminded(day: LocalDate, templateIds: Collection<String> = emptyList()) =
        prefs.edit().putString(LAST, day.toString()).putStringSet(REMINDED_IDS, templateIds.toSet()).apply()

    /** Templates the reminder on screen is about, if it went out on [day]; empty — none or an older one. */
    fun remindedFor(day: LocalDate): Set<String> = if (remindedOn(day)) prefs.getStringSet(REMINDED_IDS, null).orEmpty() else emptySet()

    /** What the reminder on screen is about now: fewer templates, or none once it is taken back. */
    fun updateReminded(templateIds: Collection<String>) = prefs.edit().putStringSet(REMINDED_IDS, templateIds.toSet()).apply()

    /** «Завтра» on the reminder: [templateIds] are asked again on [day]. */
    fun snooze(templateIds: Collection<String>, day: LocalDate) =
        prefs.edit().putStringSet(SNOOZED_IDS, templateIds.toSet()).putString(SNOOZED_DAY, day.toString()).apply()

    /** Templates put off till [day]; an older «Завтра» has done its job. */
    fun snoozedFor(day: LocalDate): Set<String> =
        if (prefs.getString(SNOOZED_DAY, null) == day.toString()) prefs.getStringSet(SNOOZED_IDS, null).orEmpty() else emptySet()

    /** The early reminder («Предупреждать за») for the payments of [day] already went out. */
    fun aheadDone(day: LocalDate): Boolean = prefs.getString(AHEAD, null) == day.toString()

    fun markAhead(day: LocalDate) = prefs.edit().putString(AHEAD, day.toString()).apply()

    private companion object {
        const val AHEAD = "ahead_for"

        const val LAST = "last_reminded"
        const val REMINDED_IDS = "reminded_ids"
        const val SNOOZED_IDS = "snoozed_ids"
        const val SNOOZED_DAY = "snoozed_day"
    }
}

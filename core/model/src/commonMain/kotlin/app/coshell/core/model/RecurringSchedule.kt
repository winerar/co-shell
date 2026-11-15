package app.coshell.core.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** How often a regular payment comes (#143): every month on a day, every week on a weekday, once a year. */
sealed interface Recurrence {
    /** Every month on [day] (1–31); a month shorter than [day] uses its last day («31-е → 28 февраля»). */
    data class Monthly(val day: Int) : Recurrence {
        init {
            require(day in 1..31) { "Day of month must be 1..31, got $day" }
        }
    }

    data class Weekly(val dayOfWeek: DayOfWeek) : Recurrence

    /** Once a year on [day] of [month] (1–12); 29 February falls on 28 February in a common year. */
    data class Yearly(val month: Int, val day: Int) : Recurrence {
        init {
            require(month in 1..12) { "Month must be 1..12, got $month" }
            require(day in 1..MONTH_MAX[month - 1]) { "No day $day in month $month" }
        }
    }

    private companion object {
        val MONTH_MAX = intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    }
}

/** What happens on the payment day: remind and wait for «Провести», or post by itself. */
enum class PostMode { REMIND, AUTO }

/**
 * The «Повтор» of a template (#143, owner picked А: a tile in the template editor).
 *
 * Occurrences start on [start] (the first one is the first matching day on or after it). [handledThrough]
 * — the last occurrence that was posted or skipped: everything on or before it is done, so a missed
 * payment waits («копятся, не теряются») until it is posted or skipped.
 */
data class RecurringSchedule(
    val recurrence: Recurrence,
    val start: LocalDate,
    val mode: PostMode = PostMode.REMIND,
    val handledThrough: LocalDate? = null,
)

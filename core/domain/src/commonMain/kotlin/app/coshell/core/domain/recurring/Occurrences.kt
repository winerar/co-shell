package app.coshell.core.domain.recurring

import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** The first payment day of this recurrence on or after [date]. */
fun Recurrence.firstOnOrAfter(date: LocalDate): LocalDate = when (this) {
    is Recurrence.Monthly -> monthDay(date.year, date.month.ordinal + 1, day).let { inMonth ->
        if (inMonth >= date) inMonth else nextMonth(date).let { (y, m) -> monthDay(y, m, day) }
    }
    is Recurrence.Weekly -> date.plus(DatePeriod(days = (dayOfWeek.ordinal - date.dayOfWeek.ordinal + 7) % 7))
    is Recurrence.Yearly -> monthDay(date.year, month, day).let { thisYear ->
        if (thisYear >= date) thisYear else monthDay(date.year + 1, month, day)
    }
}

/** Payment days of this recurrence in [from]..[to], both inclusive; at most [limit] of them. */
fun Recurrence.between(from: LocalDate, to: LocalDate, limit: Int = MAX_OCCURRENCES): List<LocalDate> {
    val days = mutableListOf<LocalDate>()
    var next = firstOnOrAfter(from)
    while (next <= to && days.size < limit) {
        days += next
        next = firstOnOrAfter(next.plus(DatePeriod(days = 1)))
    }
    return days
}

/** The first payment not posted or skipped yet. */
fun RecurringSchedule.nextUnhandled(): LocalDate {
    val from = handledThrough?.plus(DatePeriod(days = 1))?.takeIf { it > start } ?: start
    return recurrence.firstOnOrAfter(from)
}

/** Payments up to [today] that wait for «Провести» or «Пропустить», oldest first. */
fun RecurringSchedule.due(today: LocalDate): List<LocalDate> = recurrence.between(nextUnhandled(), today)

/** After posting or skipping every payment on or before [date]. Never moves back. */
fun RecurringSchedule.handledUpTo(date: LocalDate): RecurringSchedule =
    copy(handledThrough = maxOf(handledThrough ?: date, date))

/** Days from [today] to [date]: negative — overdue. */
internal fun daysFrom(today: LocalDate, date: LocalDate): Int = today.daysUntil(date)

/** A year of weekly payments: more than that means the rule was forgotten — the list shows the count, not each one. */
const val MAX_OCCURRENCES = 60

private fun monthDay(year: Int, month: Int, day: Int): LocalDate {
    val (ny, nm) = nextMonth(LocalDate(year, month, 1))
    val last = LocalDate(ny, nm, 1).minus(DatePeriod(days = 1)).day
    return LocalDate(year, month, minOf(day, last))
}

private fun nextMonth(date: LocalDate): Pair<Int, Int> {
    val m = date.month.ordinal + 1
    return if (m == 12) date.year + 1 to 1 else date.year to m + 1
}

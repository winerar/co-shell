package app.coshell.core.domain.search

import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * A period of the «Период» filter on «Операции» (#141, board `Fb141APeriod`): a month, three months, a year
 * or any days. «Раньше» / «Позже» ([shifted]) move it by its own length: a month to the previous month,
 * three months by three, a year by a year, own days by as many days.
 */
data class PeriodRange(
    val start: LocalDate,
    /** The day after the last one. */
    val endExclusive: LocalDate,
    val step: Step,
) {
    init {
        require(start < endExclusive) { "Empty period $start..$endExclusive" }
    }

    enum class Step { MONTH, THREE_MONTHS, YEAR, DAYS }

    val lastDay: LocalDate get() = endExclusive.minus(DatePeriod(days = 1))

    operator fun contains(day: LocalDate): Boolean = day >= start && day < endExclusive

    /** The same kind of period [steps] lengths later (negative — earlier). */
    fun shifted(steps: Int): PeriodRange = when (step) {
        Step.MONTH -> byMonths(steps)
        Step.THREE_MONTHS -> byMonths(steps * 3)
        Step.YEAR -> byMonths(steps * 12)
        Step.DAYS -> {
            val days = start.daysUntil(endExclusive) * steps
            copy(start = start.plus(DatePeriod(days = days)), endExclusive = endExclusive.plus(DatePeriod(days = days)))
        }
    }

    private fun byMonths(months: Int) =
        copy(start = start.plus(DatePeriod(months = months)), endExclusive = endExclusive.plus(DatePeriod(months = months)))

    /** Start (inclusive) and end (exclusive) as instants, for Room. */
    fun bounds(timeZone: TimeZone): Pair<Instant, Instant> = start.atStartOfDayIn(timeZone) to endExclusive.atStartOfDayIn(timeZone)

    /** Which ready-made button this period is today, if any (lit in the sheet). */
    fun presetOn(today: LocalDate): Preset? = Preset.entries.firstOrNull { it.range(today) == this }

    /** Ready-made periods of the sheet, in its order. */
    enum class Preset {
        THIS_MONTH, LAST_MONTH, THREE_MONTHS, THIS_YEAR, LAST_YEAR;

        fun range(today: LocalDate): PeriodRange {
            val month = LocalDate(today.year, today.month, 1)
            val year = LocalDate(today.year, 1, 1)
            return when (this) {
                THIS_MONTH -> PeriodRange(month, month.plus(DatePeriod(months = 1)), Step.MONTH)
                LAST_MONTH -> PeriodRange(month.minus(DatePeriod(months = 1)), month, Step.MONTH)
                THREE_MONTHS -> PeriodRange(month.minus(DatePeriod(months = 2)), month.plus(DatePeriod(months = 1)), Step.THREE_MONTHS)
                THIS_YEAR -> PeriodRange(year, year.plus(DatePeriod(years = 1)), Step.YEAR)
                LAST_YEAR -> PeriodRange(year.minus(DatePeriod(years = 1)), year, Step.YEAR)
            }
        }
    }

    companion object {
        /**
         * Own period from [first] to [last], both included; swapped if picked backwards. Whole calendar
         * months step by months (1–31 Oct → «Позже» is November, not 31 days on).
         */
        fun days(first: LocalDate, last: LocalDate): PeriodRange {
            val (a, b) = if (first <= last) first to last else last to first
            val end = b.plus(DatePeriod(days = 1))
            val wholeMonths = a.day == 1 && end.day == 1
            val months = (end.year - a.year) * 12 + (end.month.ordinal - a.month.ordinal)
            val step = when {
                !wholeMonths -> Step.DAYS
                months == 1 -> Step.MONTH
                months == 3 -> Step.THREE_MONTHS
                months == 12 && a.month.ordinal == 0 -> Step.YEAR
                else -> Step.DAYS
            }
            return PeriodRange(a, end, step)
        }
    }
}

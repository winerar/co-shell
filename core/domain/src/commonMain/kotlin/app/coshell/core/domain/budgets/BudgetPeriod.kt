package app.coshell.core.domain.budgets

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * The days a budget month covers: from [start] to [endExclusive]. With the month starting on the 1st it is
 * the calendar month; with another start day (e.g. payday) the period is named after the month it starts in.
 */
data class BudgetPeriod(val month: YearMonth, val start: LocalDate, val endExclusive: LocalDate) {
    val days: Int get() = start.daysUntil(endExclusive)
    val lastDay: LocalDate get() = endExclusive.minus(DatePeriod(days = 1))

    operator fun contains(day: LocalDate): Boolean = day >= start && day < endExclusive

    /** Days gone by [today], today included: 0 before the period, [days] after it. */
    fun daysPassed(today: LocalDate): Int = (start.daysUntil(today) + 1).coerceIn(0, days)

    /** Days still ahead after [today]. */
    fun daysLeft(today: LocalDate): Int = days - daysPassed(today)

    fun previous(startDay: Int): BudgetPeriod = budgetPeriod(month.minus(1, DateTimeUnit.MONTH), startDay)
}

const val DEFAULT_MONTH_START_DAY = 1

/** The period of [month]. A start day the month does not have (31st in June) falls on its last day. */
fun budgetPeriod(month: YearMonth, startDay: Int = DEFAULT_MONTH_START_DAY): BudgetPeriod {
    require(startDay in 1..31) { "Month start day must be 1..31, got $startDay" }
    return BudgetPeriod(month, startOf(month, startDay), startOf(month.plus(1, DateTimeUnit.MONTH), startDay))
}

/** The period that contains [day]. */
fun budgetPeriodOf(day: LocalDate, startDay: Int = DEFAULT_MONTH_START_DAY): BudgetPeriod {
    val current = budgetPeriod(YearMonth(day.year, day.month), startDay)
    return if (day in current) current else current.previous(startDay)
}

private fun startOf(month: YearMonth, startDay: Int): LocalDate =
    LocalDate(month.year, month.month, minOf(startDay, month.numberOfDays))

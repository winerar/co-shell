package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.budgets.DEFAULT_MONTH_START_DAY
import app.coshell.core.domain.budgets.budgetPeriod
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** How a report period is chosen in «Период и валюта» (board `RepPeriod`). */
enum class ReportPeriodKind { MONTH, QUARTER, YEAR, CUSTOM }

/**
 * The days a report covers (#140): [start] ..< [endExclusive]. A month, a quarter and a year are the family's months
 * (the month start day, as in budgets): a quarter is three of them from [firstMonth], a year — twelve from January.
 * A custom period is any run of days.
 */
data class ReportPeriod(
    val kind: ReportPeriodKind,
    val start: LocalDate,
    val endExclusive: LocalDate,
    /** The first month of a month, quarter or year; for a custom period — the month of [start]. */
    val firstMonth: YearMonth,
) {
    init {
        require(start < endExclusive) { "A report period needs at least one day: $start ..< $endExclusive" }
    }

    val days: Int get() = start.daysUntil(endExclusive)
    val lastDay: LocalDate get() = endExclusive.minus(DatePeriod(days = 1))

    /** The last month of the period (for a custom one — the month of [lastDay]). */
    val lastMonth: YearMonth
        get() = when (kind) {
            ReportPeriodKind.MONTH -> firstMonth
            ReportPeriodKind.QUARTER -> firstMonth.plus(2, DateTimeUnit.MONTH)
            ReportPeriodKind.YEAR -> firstMonth.plus(11, DateTimeUnit.MONTH)
            ReportPeriodKind.CUSTOM -> YearMonth(lastDay.year, lastDay.month)
        }

    /** 1..4 for a quarter. */
    val quarter: Int get() = (firstMonth.month.ordinal / MONTHS_IN_QUARTER) + 1

    operator fun contains(day: LocalDate): Boolean = day >= start && day < endExclusive

    /** Days gone by [today], today included: 0 before the period, [days] after it. */
    fun daysPassed(today: LocalDate): Int = (start.daysUntil(today) + 1).coerceIn(0, days)

    /** Whether [today] is still inside, i.e. the period is not over. */
    fun isRunning(today: LocalDate): Boolean = today in this

    /** The period of the same kind right before: the month, quarter or year before; a custom one — as many days before. */
    fun previous(startDay: Int = DEFAULT_MONTH_START_DAY): ReportPeriod = shift(-1, startDay)

    /** The period [by] steps later (negative — earlier): ‹ and › next to the period name. */
    fun shift(by: Int, startDay: Int = DEFAULT_MONTH_START_DAY): ReportPeriod {
        if (by == 0) return this
        return when (kind) {
            ReportPeriodKind.MONTH -> month(firstMonth.shiftMonths(by), startDay)
            ReportPeriodKind.QUARTER -> quarter(firstMonth.shiftMonths(by * MONTHS_IN_QUARTER), startDay)
            ReportPeriodKind.YEAR -> year(firstMonth.year + by, startDay)
            ReportPeriodKind.CUSTOM -> {
                val step = DatePeriod(days = days * by)
                custom(start.plus(step), lastDay.plus(step))
            }
        }
    }

    /**
     * The months of «По месяцам»: six months up to a month (or a quarter — two quarters side by side), the twelve
     * months of a year, the months a custom period touches (at most the last twelve). Oldest first.
     */
    fun columnMonths(startDay: Int = DEFAULT_MONTH_START_DAY): List<BudgetPeriod> {
        val count = when (kind) {
            ReportPeriodKind.MONTH, ReportPeriodKind.QUARTER -> RECENT_MONTHS
            ReportPeriodKind.YEAR -> MONTHS_IN_YEAR
            ReportPeriodKind.CUSTOM -> monthsBetween(firstMonth, lastMonth).coerceIn(1, MONTHS_IN_YEAR)
        }
        return reportMonths(lastMonth, count, startDay)
    }

    companion object {
        const val MONTHS_IN_QUARTER = 3
        const val MONTHS_IN_YEAR = 12

        /** Columns shown for a month: half a year is enough to see a trend and still reads at 390 dp. */
        const val RECENT_MONTHS = 6

        fun month(month: YearMonth, startDay: Int = DEFAULT_MONTH_START_DAY): ReportPeriod {
            val p = budgetPeriod(month, startDay)
            return ReportPeriod(ReportPeriodKind.MONTH, p.start, p.endExclusive, month)
        }

        /** The quarter that holds [anyMonth]. */
        fun quarter(anyMonth: YearMonth, startDay: Int = DEFAULT_MONTH_START_DAY): ReportPeriod {
            val first = YearMonth(anyMonth.year, (anyMonth.month.ordinal / MONTHS_IN_QUARTER) * MONTHS_IN_QUARTER + 1)
            val last = first.plus(MONTHS_IN_QUARTER - 1, DateTimeUnit.MONTH)
            return ReportPeriod(ReportPeriodKind.QUARTER, budgetPeriod(first, startDay).start, budgetPeriod(last, startDay).endExclusive, first)
        }

        fun year(year: Int, startDay: Int = DEFAULT_MONTH_START_DAY): ReportPeriod {
            val first = YearMonth(year, 1)
            return ReportPeriod(ReportPeriodKind.YEAR, budgetPeriod(first, startDay).start, budgetPeriod(YearMonth(year, MONTHS_IN_YEAR), startDay).endExclusive, first)
        }

        /** [from] .. [toInclusive], swapped if given the other way round. */
        fun custom(from: LocalDate, toInclusive: LocalDate): ReportPeriod {
            val (a, b) = if (from <= toInclusive) from to toInclusive else toInclusive to from
            return ReportPeriod(ReportPeriodKind.CUSTOM, a, b.plus(DatePeriod(days = 1)), YearMonth(a.year, a.month))
        }

        /** The budget month as a report month. */
        fun of(period: BudgetPeriod): ReportPeriod = ReportPeriod(ReportPeriodKind.MONTH, period.start, period.endExclusive, period.month)
    }
}

private fun YearMonth.shiftMonths(by: Int): YearMonth = if (by >= 0) plus(by, DateTimeUnit.MONTH) else minus(-by, DateTimeUnit.MONTH)

/** Months from [first] to [last], both counted. */
private fun monthsBetween(first: YearMonth, last: YearMonth): Int =
    (last.year - first.year) * ReportPeriod.MONTHS_IN_YEAR + (last.month.ordinal - first.month.ordinal) + 1

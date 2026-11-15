package app.coshell.core.domain.reports

import kotlinx.datetime.LocalDate

/**
 * «Доходы и расходы» (#189, board `Rp189IeA`): what came in and went out each month, what was left, and the average
 * savings rate of the finished months — a running month is «пока» and would pull the average up (salary in, the
 * spending still ahead).
 */
data class IncomeExpenses(
    val months: List<MonthTotals>,
    /** [MonthTotals.saved] of the finished months in percents of their income; `null` — none finished or nothing came in. */
    val averageRate: Int?,
    /** How many finished months with operations [averageRate] is counted over. */
    val averageMonths: Int,
) {
    val isEmpty: Boolean get() = months.none { it.spent.isPositive || it.earned.isPositive }
}

/** True while [today] is inside the month: its numbers are not final yet. */
fun MonthTotals.isRunning(today: LocalDate): Boolean = today < period.endExclusive && today >= period.start

/** [series] — `monthlySeries` of the shown months; the average skips months not over by [today] and months without operations. */
fun incomeExpenses(series: List<MonthTotals>, today: LocalDate): IncomeExpenses {
    val finished = series.filter { today >= it.period.endExclusive && (it.spent.isPositive || it.earned.isPositive) }
    val rate = finished.takeIf { it.isNotEmpty() }?.let { months ->
        val earned = months.map { it.earned }.reduce { a, b -> a + b }
        val saved = months.map { it.saved }.reduce { a, b -> a + b }
        percentOf(saved, earned)
    }
    return IncomeExpenses(series, rate, finished.size)
}

package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.budgets.DEFAULT_MONTH_START_DAY
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus

/** One column pair of «Расходы и доходы по месяцам». [missing] — currencies left out for lack of a rate. */
data class MonthTotals(
    val period: BudgetPeriod,
    val spent: Money,
    val earned: Money,
    val missing: Set<CurrencyCode> = emptySet(),
) {
    /** What was left over: [earned] − [spent]; negative — spent more than came in. */
    val saved: Money get() = earned - spent
}

/** The [count] months ending with [last] (oldest first), in the family's months ([startDay]). */
fun reportMonths(last: YearMonth, count: Int, startDay: Int = DEFAULT_MONTH_START_DAY): List<BudgetPeriod> {
    require(count > 0) { "count must be positive" }
    return (count - 1 downTo 0).map { budgetPeriod(last.minus(it, DateTimeUnit.MONTH), startDay) }
}

/**
 * Spending and income of each of [periods] (#187). Load the days with [ReportRepository.observeDays] from the
 * first period's start to the last one's end — a year of day sums, not a year of operations.
 */
fun monthlySeries(ledger: ReportLedger, periods: List<BudgetPeriod>): List<MonthTotals> = periods.map { period ->
    val spent = ledger.spent(period)
    val earned = ledger.earned(period)
    MonthTotals(period, spent.total(ledger.currency), earned.total(ledger.currency), spent.missing + earned.missing)
}

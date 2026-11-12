package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.model.Money
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * «В октябре к 11-му было 820» (#187): spending of [period] so far against the same number of days of the
 * period before. [sameDay] — the last day of the previous period that is counted («к 11-му»).
 */
data class PeriodComparison(
    val period: BudgetPeriod,
    val spent: Money,
    val previous: BudgetPeriod,
    val previousSameDays: Money,
    val sameDay: LocalDate,
    val previousTotal: Money,
) {
    /** [spent] − [previousSameDays]: positive — spending faster than last month. */
    val difference: Money get() = spent - previousSameDays
}

/**
 * Compares [period] up to [today] (the whole period once it is over) with [previous]. The previous period is cut
 * at as many days as have passed, never longer than it is (31 days in March against 28 in February).
 * The ledger must hold the days of both periods.
 */
fun periodComparison(ledger: ReportLedger, period: BudgetPeriod, previous: BudgetPeriod, today: LocalDate): PeriodComparison {
    val passed = period.daysPassed(today)
    val currency = ledger.currency
    val spent = ledger.spent(period.start, period.start.plus(DatePeriod(days = passed))).total(currency)
    val previousDays = passed.coerceAtMost(previous.days)
    val previousEnd = previous.start.plus(DatePeriod(days = previousDays))
    return PeriodComparison(
        period = period,
        spent = spent,
        previous = previous,
        previousSameDays = ledger.spent(previous.start, previousEnd).total(currency),
        sameDay = previousEnd.minus(DatePeriod(days = 1)),
        previousTotal = ledger.spent(previous).total(currency),
    )
}

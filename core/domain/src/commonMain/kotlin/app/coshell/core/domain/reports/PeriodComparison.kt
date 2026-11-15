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
    val period: ReportPeriod,
    val spent: Money,
    val previous: ReportPeriod,
    val previousSameDays: Money,
    val sameDay: LocalDate,
    val previousTotal: Money,
    /** Days of [period] counted in [spent]: all of them once it is over. */
    val daysPassed: Int,
) {
    /** [spent] − [previousSameDays]: positive — spending faster than last period. */
    val difference: Money get() = spent - previousSameDays

    /** The period is over: [spent] is its whole spending, compared with the whole [previousTotal]. */
    val isComplete: Boolean get() = daysPassed == period.days
}

/**
 * Compares [period] up to [today] (the whole period once it is over) with [previous]. The previous period is cut
 * at as many days as have passed, never longer than it is (31 days in March against 28 in February).
 * The ledger must hold the days of both periods.
 */
fun periodComparison(ledger: ReportLedger, period: ReportPeriod, previous: ReportPeriod, today: LocalDate): PeriodComparison {
    val passed = period.daysPassed(today)
    val currency = ledger.currency
    val spent = ledger.spent(period.start, period.start.plus(DatePeriod(days = passed))).total(currency)
    val previousDays = if (passed == period.days) previous.days else passed.coerceAtMost(previous.days)
    val previousEnd = previous.start.plus(DatePeriod(days = previousDays))
    return PeriodComparison(
        period = period,
        spent = spent,
        previous = previous,
        previousSameDays = ledger.spent(previous.start, previousEnd).total(currency),
        sameDay = previousEnd.minus(DatePeriod(days = 1)),
        previousTotal = ledger.spent(previous.start, previous.endExclusive).total(currency),
        daysPassed = passed,
    )
}

/** [periodComparison] of two budget months. */
fun periodComparison(ledger: ReportLedger, period: BudgetPeriod, previous: BudgetPeriod, today: LocalDate): PeriodComparison =
    periodComparison(ledger, ReportPeriod.of(period), ReportPeriod.of(previous), today)

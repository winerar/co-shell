package app.coshell.core.domain.budgets

import app.coshell.core.model.Budget
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/**
 * [FAST] — within the limit, but spending is ahead of the days gone by more than [PACE_TOLERANCE_PERCENT];
 * [OVER] — over the limit.
 */
enum class BudgetState { OK, FAST, OVER }

/** Spending a little ahead of the pace is not a warning yet: the mark would flicker on every purchase. */
const val PACE_TOLERANCE_PERCENT = 10

/** A forecast for the end of the month is shown only after this many days: earlier it jumps too much. */
const val FORECAST_MIN_DAYS = 3

/** How one limit is doing in [period], all amounts in the currency of the limit. */
data class BudgetProgress(
    val budget: Budget,
    val period: BudgetPeriod,
    val spent: Money,
    /** Left (+) or overspent (−) last month and carried over; zero unless the limit has rollover. */
    val carried: Money,
    /** The limit plus [carried]: what the month really has. */
    val available: Money,
    /** [available] − [spent]; negative — overspent by that much. */
    val left: Money,
    val state: BudgetState,
    /** "By today it should be ≈ X"; `null` when the period is not the current one. */
    val paceToday: Money?,
    /** Spending by the end of the period at the current rate; `null` — not current or fewer than [FORECAST_MIN_DAYS] days in. */
    val forecast: Money?,
    /** Days ahead after today; 0 for a past period, all of them for a future one. */
    val daysLeft: Int,
    /** "Y a day" for the days ahead; `null` — nothing left, last day, or not the current period. */
    val perDay: Money?,
    /** Currencies of operations left out for lack of a rate. */
    val missing: Set<CurrencyCode> = emptySet(),
)

/** The numbers of one limit from what was [spent] in [period] and what was [carried] into it. */
fun budgetProgress(budget: Budget, period: BudgetPeriod, today: LocalDate, spent: Spent, carried: Money): BudgetProgress {
    val available = budget.limit + carried
    val left = available - spent.sum
    val current = today in period
    val passed = period.daysPassed(today).toLong()
    val daysLeft = period.daysLeft(today)
    val pace = if (current && available.isPositive) available.scale(passed, period.days.toLong()) else null
    return BudgetProgress(
        budget = budget,
        period = period,
        spent = spent.sum,
        carried = carried,
        available = available,
        left = left,
        state = when {
            spent.sum > available -> BudgetState.OVER
            pace != null && spent.sum > pace.scale(100L + PACE_TOLERANCE_PERCENT, 100) -> BudgetState.FAST
            else -> BudgetState.OK
        },
        paceToday = pace,
        forecast = forecast(spent.sum, period, today),
        daysLeft = daysLeft,
        perDay = if (current && daysLeft > 0 && left.isPositive) left.scale(1, daysLeft.toLong()) else null,
        missing = spent.missing,
    )
}

/** [spent] stretched from the days gone to the whole period; `null` — see [BudgetProgress.forecast]. */
fun forecast(spent: Money, period: BudgetPeriod, today: LocalDate): Money? {
    val passed = period.daysPassed(today)
    if (today !in period || passed < FORECAST_MIN_DAYS || spent.isNegative) return null
    return spent.scale(period.days.toLong(), passed.toLong())
}

package app.coshell.core.domain.budgets

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** The whole month in one currency: planned / spent / left / forecast. */
data class BudgetMonthSummary(
    val period: BudgetPeriod,
    /** Sum of the limits with what was carried into them. */
    val planned: Money,
    val spent: Money,
    /** [planned] − [spent]; negative — overspent. */
    val left: Money,
    /** See [BudgetProgress.forecast]. */
    val forecast: Money?,
    /** Currencies left out of the sums for lack of a rate. */
    val missing: Set<CurrencyCode> = emptySet(),
)

/**
 * Plan against fact for the limits of a month. [budgets] — all limit records, [categories] — all
 * categories with archived ones (a folder limit covers its subcategories), [startDay] — the day the
 * family's month starts on.
 */
class BudgetCalculator(
    private val budgets: List<Budget>,
    private val categories: List<Category>,
    private val ledger: BudgetLedger,
    private val book: RateBook,
    private val startDay: Int = DEFAULT_MONTH_START_DAY,
) {
    private val recordsByCategory = budgets.groupBy { it.categoryId }

    /** Every limit of [month], in no particular order. */
    fun month(month: YearMonth, today: LocalDate): List<BudgetProgress> =
        budgets.inMonth(month).map { progress(it, month, today) }

    fun progress(categoryId: String, month: YearMonth, today: LocalDate): BudgetProgress? =
        limit(categoryId, month)?.let { progress(it, month, today) }

    /**
     * Totals of [month] in [currency]. A category limit inside a folder that has its own limit is a
     * part of the folder's and is not added twice. Limits are converted at today's rate (the last day
     * for a past month, the first for a future one), spending — at the rate of each operation's day.
     */
    fun summary(month: YearMonth, today: LocalDate, currency: CurrencyCode): BudgetMonthSummary {
        val period = budgetPeriod(month, startDay)
        val inMonth = budgets.inMonth(month)
        val limited = inMonth.map { it.categoryId }.toSet()
        val parents = categories.associate { it.id to it.parentId }
        val top = inMonth.filter { parents[it.categoryId] !in limited }

        val rateDay = today.coerceIn(period.start, period.lastDay)
        var planned = Money.zero(currency)
        val missing = linkedSetOf<CurrencyCode>()
        for (budget in top) {
            val available = budget.limit + carried(budget, month)
            val rate = book.find(available.currency, currency, rateDay)?.rate
            if (rate == null) missing += available.currency else planned += rate.convert(available, currency)
        }
        val spent = ledger.spent(top.flatMapTo(mutableSetOf()) { covered(it) }, period, currency)
        return BudgetMonthSummary(
            period = period,
            planned = planned,
            spent = spent.sum,
            left = planned - spent.sum,
            forecast = forecast(spent.sum, period, today),
            missing = missing + spent.missing,
        )
    }

    private fun progress(budget: Budget, month: YearMonth, today: LocalDate): BudgetProgress {
        val period = budgetPeriod(month, startDay)
        val spent = ledger.spent(covered(budget), period, budget.limit.currency)
        return budgetProgress(budget, period, today, spent, carried(budget, month))
    }

    /**
     * What the limit [current] brings into [month] from the months before: the rest (or the
     * overspending) of every month in a row that has rollover on, up to [month] itself. Turning rollover
     * on starts a new row from that month; turning it off drops the carried amount at once. When the
     * limit changes currency the amount is converted on the first day of the new month.
     */
    private fun carried(current: Budget, month: YearMonth): Money {
        val categoryId = current.categoryId
        var first = month
        while (true) {
            val previous = first.minus(1, DateTimeUnit.MONTH)
            if (limit(categoryId, first)?.rollover != true || limit(categoryId, previous)?.rollover != true) break
            first = previous
        }
        var carried = Money.zero(limit(categoryId, first)!!.limit.currency)
        var at = first
        while (at < month) {
            val budget = limit(categoryId, at)!!
            val period = budgetPeriod(at, startDay)
            val left = budget.limit + carried - ledger.spent(covered(budget), period, budget.limit.currency).sum
            at = at.plus(1, DateTimeUnit.MONTH)
            val next = limit(categoryId, at)!!.limit.currency
            carried = book.find(left.currency, next, period.endExclusive)?.rate?.convert(left, next) ?: Money.zero(next)
        }
        check(carried.currency == current.limit.currency)
        return carried
    }

    private fun limit(categoryId: String, month: YearMonth): Budget? =
        recordsByCategory[categoryId].orEmpty().inMonth(month).singleOrNull()

    private fun covered(budget: Budget): Set<String> = budgetCategoryIds(budget.categoryId, categories)
}

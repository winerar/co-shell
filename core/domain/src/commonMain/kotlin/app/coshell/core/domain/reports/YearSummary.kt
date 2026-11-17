package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money

/**
 * The year in numbers (#239): [spent], [earned] and what was left ([saved] = earned − spent) over [months] months;
 * [averageSpent] — spending of a month on average; [topFolders] — up to [TOP_FOLDERS] folders that spent most, largest
 * first; [priciest] — the month that spent most (the earlier one on a tie), `null` when nothing was spent.
 * [missing] — currencies left out for lack of a rate.
 */
data class YearSummary(
    val months: Int,
    val spent: Money,
    val earned: Money,
    val averageSpent: Money,
    val topFolders: List<Pair<String, Money>>,
    val priciest: MonthTotals?,
    val missing: Set<CurrencyCode> = emptySet(),
) {
    val saved: Money get() = earned - spent
}

/** The summary of [periods] (a year of months, oldest first; the ledger must hold all their days). */
fun yearSummary(ledger: ReportLedger, categories: List<Category>, periods: List<BudgetPeriod>): YearSummary {
    require(periods.isNotEmpty()) { "periods must not be empty" }
    val currency = ledger.currency
    val series = monthlySeries(ledger, periods)
    val spent = series.fold(Money.zero(currency)) { acc, it -> acc + it.spent }
    val earned = series.fold(Money.zero(currency)) { acc, it -> acc + it.earned }
    val whole = ledger.spent(periods.first().start, periods.last().endExclusive)
    val top = spendingByFolder(whole, categories, currency).entries.filter { it.value.isPositive }
        .sortedWith(compareByDescending<Map.Entry<String, Money>> { it.value.amountMinor }.thenBy { it.key })
        .take(TOP_FOLDERS).map { it.key to it.value }
    val priciest = series.filter { it.spent.isPositive }.maxByOrNull { it.spent.amountMinor }
    return YearSummary(
        periods.size, spent, earned, spent.scale(1, periods.size.toLong()), top, priciest,
        series.flatMapTo(linkedSetOf()) { it.missing },
    )
}

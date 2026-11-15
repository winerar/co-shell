package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/** One column of a category's trend; [amount] is `null` before the family's history starts (nothing to draw). */
data class TrendMonth(val period: BudgetPeriod, val amount: Money?)

/**
 * A category or folder over a year (#237, for #188 and «Итог месяца»): [months] — the [TREND_MONTHS] months ending
 * with the chosen one (oldest first; the last one so far), the average spending of the [USUAL_MONTHS] and of the
 * [LONG_USUAL_MONTHS] full months before it (`null` without history), and the [mark] «выше / ниже обычного».
 * [missing] — currencies left out for lack of a rate.
 */
data class CategoryTrend(
    val months: List<TrendMonth>,
    val average3: Money?,
    val average12: Money?,
    val mark: UsualMark?,
    val missing: Set<CurrencyCode>,
)

/** Columns of the trend chart. */
const val TREND_MONTHS = 12

/** The long average — the line across the trend chart. */
const val LONG_USUAL_MONTHS = 12

/** The categories a trend of [categoryId] sums: a folder — itself and its subcategories; a category — itself. */
fun trendCategoryIds(categoryId: String, categories: List<Category>): Set<String> =
    setOf(categoryId) + categories.filter { it.parentId == categoryId }.map { it.id }

/**
 * The trend of [categoryIds] for [month] up to [today]. Refunds above the spending count as zero. The ledger must
 * hold [month] and the [LONG_USUAL_MONTHS] months before it — a year of day sums. Months before the first one with
 * any operations (of any category) are before the history and count neither in the averages nor on the chart.
 */
fun categoryTrend(
    ledger: ReportLedger,
    categoryIds: Set<String>,
    month: BudgetPeriod,
    startDay: Int,
    today: LocalDate,
): CategoryTrend {
    val currency = ledger.currency
    val missing = linkedSetOf<CurrencyCode>()
    fun spentIn(period: BudgetPeriod): Money {
        val sums = ledger.spent(period)
        missing += sums.missing
        return sums.byCategory.filterKeys { it in categoryIds }.values
            .fold(Money.zero(currency)) { acc, it -> acc + it }.atLeastZero()
    }

    val before = usualHistory(ledger, monthsBefore(month, startDay, LONG_USUAL_MONTHS))
    val amounts = (before + month).associateWith { spentIn(it) }
    val chart = reportMonths(month.month, TREND_MONTHS, startDay).map { TrendMonth(it, amounts[it]) }

    val recent = before.takeLast(USUAL_MONTHS)
    val recentAmounts = recent.map { amounts.getValue(it) }
    val mark = if (recent.isEmpty()) null
    else usualMark(amounts.getValue(month), recentAmounts, usualTotal(ledger, recent), month, today)

    return CategoryTrend(
        months = chart,
        average3 = recentAmounts.averageOrNull(currency),
        average12 = before.map { amounts.getValue(it) }.averageOrNull(currency),
        mark = mark,
        missing = missing,
    )
}

private fun List<Money>.averageOrNull(currency: CurrencyCode): Money? =
    if (isEmpty()) null else fold(Money.zero(currency)) { acc, it -> acc + it }.scale(1, size.toLong())

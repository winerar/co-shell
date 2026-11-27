package app.coshell.core.domain.budgets

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Currencies
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import kotlinx.datetime.YearMonth

/** A limit offered on the first visit to «Бюджеты» (#185, board `BudFirst`): the category or folder and a round amount. */
data class BudgetSuggestion(val categoryId: String, val limit: Money)

/**
 * The limits to offer a family that has none yet: for every expense category that has no parent (a folder covers its subcategories,
 * [budgetCategoryIds]) the average monthly spending over the [months] periods before [month] ([BudgetLedger.averageSpent]), in
 * [currency], rounded to a round number ([roundedLimit]). Categories nothing was spent on are left out; the biggest limit comes first.
 */
fun suggestBudgetLimits(
    ledger: BudgetLedger,
    categories: List<Category>,
    month: YearMonth,
    currency: CurrencyCode,
    startDay: Int = DEFAULT_MONTH_START_DAY,
    months: Int = 3,
): List<BudgetSuggestion> = categories
    .filter { it.parentId == null && it.type == CategoryType.EXPENSE && !it.archived && !SystemCategories.isAdjustment(it.id) }
    .mapNotNull { category ->
        val average = ledger.averageSpent(budgetCategoryIds(category.id, categories), month, currency, startDay, months) ?: return@mapNotNull null
        BudgetSuggestion(category.id, roundedLimit(average))
    }
    .sortedByDescending { it.limit.amountMinor }

/**
 * A limit is a figure to remember, not an accounting entry: whole units of the currency, and from 1000 up only two digits stay
 * («371 840» → «370 000», «78 400» → «78 000», «1 450» → «1 500»). Never below one unit — a limit of zero means «not planned», not this.
 */
fun roundedLimit(average: Money): Money {
    val unit = Currencies.info(average.currency).minorPerMajor
    var major = maxOf(1L, (average.amountMinor + unit / 2) / unit)
    if (major >= ROUND_FROM) {
        var step = 1L
        repeat(major.toString().length - KEPT_DIGITS) { step *= 10 }
        major = (major + step / 2) / step * step
    }
    return Money.ofMajor(major, currency = average.currency)
}

private const val ROUND_FROM = 1_000L
private const val KEPT_DIGITS = 2

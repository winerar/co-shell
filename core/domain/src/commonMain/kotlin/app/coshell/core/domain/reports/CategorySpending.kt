package app.coshell.core.domain.reports

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money

/** One row of «Все категории» (board `RepAll`): a category, its spending, [percent] of all and the period before. */
data class CategorySpending(
    val categoryId: String,
    val amount: Money,
    val percent: Int,
    val previous: Money,
) {
    /** Growth against the period before: positive — spent more. */
    val growth: Money get() = amount - previous
}

/** How «Все категории» is ordered. */
enum class CategoryOrder { BY_AMOUNT, BY_GROWTH }

/**
 * Every category that spent more than it got back in [current], not grouped by folder, with the same category's net
 * spending in [previous] (zero when it spent nothing then). Percents are of their sum and add up to 100.
 */
fun categorySpending(
    current: CategorySums,
    previous: CategorySums?,
    currency: CurrencyCode,
    order: CategoryOrder = CategoryOrder.BY_AMOUNT,
): List<CategorySpending> {
    val positive = current.byCategory.entries.filter { it.value.isPositive }
    val percents = percentShares(positive.map { it.value.amountMinor })
    val rows = positive.mapIndexed { i, (id, money) ->
        CategorySpending(id, money, percents[i], previous?.byCategory?.get(id) ?: Money.zero(currency))
    }
    val byAmount = compareByDescending<CategorySpending> { it.amount.amountMinor }.thenBy { it.categoryId }
    return when (order) {
        CategoryOrder.BY_AMOUNT -> rows.sortedWith(byAmount)
        CategoryOrder.BY_GROWTH -> rows.sortedWith(compareByDescending<CategorySpending> { it.growth.amountMinor }.then(byAmount))
    }
}

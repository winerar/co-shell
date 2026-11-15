package app.coshell.core.domain.reports

import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money

/**
 * «Из чего сложилось» (board `RepDrill`, #188): one category's net spending in a period, [percent] of all spending,
 * and its [parts] — the subcategories (and the category's own operations, under its own id), largest first, with
 * percents of the category. A category without subcategories has no parts.
 */
data class CategoryDrill(
    val categoryId: String,
    val amount: Money,
    val percent: Int,
    val parts: List<SpendingSlice>,
)

/**
 * The drill of [categoryId] in [sums]. A top-level category is counted as on «На что ушли деньги» — with its
 * subcategories, the percent among the folders; a subcategory (opened from «Все категории») — alone, the percent among
 * all categories, as in that list. `null` — the category spent no more than it got back.
 */
fun categoryDrill(sums: CategorySums, categoryId: String, categories: List<Category>, currency: CurrencyCode): CategoryDrill? {
    val byId = categories.associateBy { it.id }
    val isSubcategory = byId[categoryId]?.parentId?.let { it in byId } == true
    if (isSubcategory) {
        val row = categorySpending(sums, null, currency).firstOrNull { it.categoryId == categoryId } ?: return null
        return CategoryDrill(categoryId, row.amount, row.percent, emptyList())
    }
    // Every folder as a slice: none may go into «Другое» here.
    val slice = spendingBreakdown(sums, categories, currency, top = sums.byCategory.size)
        .slices.firstOrNull { it.categoryId == categoryId } ?: return null
    return CategoryDrill(categoryId, slice.amount, slice.percent, slice.parts)
}

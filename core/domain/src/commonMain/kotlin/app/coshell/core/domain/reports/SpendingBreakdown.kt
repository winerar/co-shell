package app.coshell.core.domain.reports

import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money

/** One bar of «На что ушли деньги»: a category or a folder, its spending and [percent] of the spending. */
data class SpendingSlice(
    val categoryId: String,
    val amount: Money,
    val percent: Int,
    /**
     * A folder's composition, largest first, with percents of the folder; the folder's own operations
     * (not in a subcategory) are the part with the folder's id. Empty for a plain category.
     */
    val parts: List<SpendingSlice> = emptyList(),
)

/** «Другое»: the slices after the largest ones — «Транспорт и ещё 3» is [slices] first and [slices] size − 1. */
data class OtherSlice(val amount: Money, val percent: Int, val slices: List<SpendingSlice>)

/**
 * The structure of spending for a period (#187). [total] — net spending, refunds taken off; [slices] — the largest
 * folders and top-level categories, largest first; [other] — the rest, or `null` when it all fits.
 * Percents are of the spending of the categories that spent more than they got back (a category with more refunds
 * than spending is not drawn), and add up to 100.
 */
data class SpendingBreakdown(
    val total: Money,
    val slices: List<SpendingSlice>,
    val other: OtherSlice?,
    val missing: Set<CurrencyCode> = emptySet(),
) {
    val isEmpty: Boolean get() = slices.isEmpty()
}

/** How many slices are shown before «Другое» (research §3: 5–7 on the screen). */
const val REPORT_TOP_SLICES = 5

/**
 * Groups [sums] by folder: a subcategory goes into its folder, a category without one stands alone. «Другое»
 * gathers everything after [top] slices, unless it would hold one slice only — that one is shown instead.
 */
fun spendingBreakdown(sums: CategorySums, categories: List<Category>, currency: CurrencyCode, top: Int = REPORT_TOP_SLICES): SpendingBreakdown {
    val byId = categories.associateBy { it.id }
    fun folderOf(id: String): String = byId[id]?.parentId?.takeIf { it in byId } ?: id

    val groups = sums.byCategory.entries.groupBy({ folderOf(it.key) }, { it.key to it.value })
    val folders = groups.map { (folderId, members) ->
        val amount = members.fold(Money.zero(currency)) { acc, (_, money) -> acc + money }
        val isFolder = members.any { (id, _) -> id != folderId } || categories.any { it.parentId == folderId }
        val parts = if (isFolder) slices(members.map { it.first to it.second }) else emptyList()
        SpendingSlice(folderId, amount, 0, parts)
    }
    val drawn = slices(folders.map { it.categoryId to it.amount }).map { slice -> folders.first { it.categoryId == slice.categoryId }.copy(percent = slice.percent) }

    val split = if (drawn.size > top + 1) top else drawn.size
    val shown = drawn.take(split)
    val rest = drawn.drop(split)
    val other = rest.takeIf { it.isNotEmpty() }?.let { list ->
        OtherSlice(list.fold(Money.zero(currency)) { acc, it -> acc + it.amount }, list.sumOf { it.percent }, list)
    }
    return SpendingBreakdown(sums.total(currency), shown, other, sums.missing)
}

/** Positive amounts as slices, largest first (ties — by id, so the order is stable), with percents of their sum. */
private fun slices(amounts: List<Pair<String, Money>>): List<SpendingSlice> {
    val positive = amounts.filter { it.second.isPositive }.sortedWith(compareByDescending<Pair<String, Money>> { it.second.amountMinor }.thenBy { it.first })
    val percents = percentShares(positive.map { it.second.amountMinor })
    return positive.mapIndexed { i, (id, money) -> SpendingSlice(id, money, percents[i]) }
}

package app.coshell.reports.all

import app.coshell.reports.ReportsUiState.CategoryRow

/** «По сумме» · «По росту» · «Группами» (board `RepAll`). */
enum class RowOrder { AMOUNT, GROWTH, GROUPS }

/** A row and whether it sits under its parent (only in [RowOrder.GROUPS]). */
data class OrderedRow(val row: CategoryRow, val inner: Boolean)

/**
 * Pure: [rows] matching [query] (in the name, any case), in [order]. Groups go by their total, largest first; in a
 * group the parent comes first, then its subcategories by amount. A subcategory whose parent spent nothing itself
 * still sits under the parent's key, without the parent's row.
 */
fun orderRows(rows: List<CategoryRow>, query: String, order: RowOrder, nameOf: (CategoryRow) -> String): List<OrderedRow> {
    val q = query.trim()
    val found = if (q.isEmpty()) rows else rows.filter { nameOf(it).contains(q, ignoreCase = true) }
    val byAmount = compareByDescending<CategoryRow> { it.amountMinor }.thenBy { it.categoryId }
    return when (order) {
        RowOrder.AMOUNT -> found.sortedWith(byAmount).map { OrderedRow(it, inner = false) }
        RowOrder.GROWTH -> found.sortedWith(compareByDescending<CategoryRow> { it.growthMinor }.then(byAmount)).map { OrderedRow(it, inner = false) }
        RowOrder.GROUPS -> found
            .groupBy { it.parentId ?: it.categoryId }
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, List<CategoryRow>>> { e -> e.value.sumOf { it.amountMinor } }.thenBy { it.key })
            .flatMap { (key, members) ->
                val parent = members.filter { it.categoryId == key }
                val children = members.filter { it.categoryId != key }.sortedWith(byAmount)
                parent.map { OrderedRow(it, inner = false) } + children.map { OrderedRow(it, inner = true) }
            }
    }
}

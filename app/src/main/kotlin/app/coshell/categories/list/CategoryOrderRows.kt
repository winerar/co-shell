package app.coshell.categories.list

import app.coshell.core.domain.order.ManualOrder.Place

/**
 * Rows of «Порядок категорий» (#338, board `Fb338Cat`) and where a drop lands. Pure — tested in `CategoryOrderRowsTest`.
 * [parentId] — the parent of a subcategory; `null` — a top-level category.
 */
internal data class OrderRow(val id: String, val name: String, val color: Long?, val icon: String?, val parentId: String?, val hasChildren: Boolean)

/** Parents with their subcategories right under them; «Корректировка баланса» is left out (it stays last). */
internal fun orderRows(groups: List<CategoriesUiState.Group>, fixed: (String) -> Boolean): List<OrderRow> =
    groups.filterNot { fixed(it.item.id) }.flatMap { g ->
        listOf(OrderRow(g.item.id, g.item.name, g.item.color, g.item.icon, null, g.children.isNotEmpty())) +
            g.children.map { OrderRow(it.id, it.name, it.color, it.icon, g.item.id, false) }
    }

/**
 * Where [movedId] lands when dropped at [index] among the other rows ([into] — dropped on the middle of a category).
 * Between subcategories — among them; elsewhere — at the top level. A category with subcategories always stays on top:
 * dropped inside another group, it goes right after that group.
 */
internal fun dropPlace(rows: List<OrderRow>, movedId: String, index: Int, into: String?): Place {
    val moved = rows.first { it.id == movedId }
    if (into != null) return Place(into, rows.count { it.parentId == into && it.id != movedId })
    val others = rows.filter { it.id != movedId && !(moved.hasChildren && it.parentId == movedId) }
    val at = index.coerceIn(0, others.size)
    val below = others.getOrNull(at)
    if (!moved.hasChildren && below?.parentId != null) {
        return Place(below.parentId, others.take(at).count { it.parentId == below.parentId })
    }
    // Top level: count the top-level rows before the drop point (a drop inside a group goes after it).
    return Place(null, others.take(at).count { it.parentId == null })
}

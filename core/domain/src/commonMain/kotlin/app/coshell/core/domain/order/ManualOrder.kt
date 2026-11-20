package app.coshell.core.domain.order

import app.coshell.core.domain.Reorder
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.SystemCategories

/**
 * What to save after a drag in «Порядок» (#338, boards `Fb338Acc`, `Fb338Cat`). Pure: the screen shows the result at
 * once, [ReorderUseCase] writes it. While a list is still alphabetical ([manual] = false), the first move stores every
 * row's place on screen, so the rest of the list stays where the person saw it; after that a move touches one row.
 */
object ManualOrder {

    /** Accounts with new `sortOrder` after moving [from] → [to] in [shown] (display order); only the changed ones. */
    fun moveAccount(shown: List<Account>, from: Int, to: Int, manual: Boolean): List<Account> {
        val orders = move(shown.map { Reorder.Item(it.id, it.sortOrder) }, from, to, manual)
        return shown.mapNotNull { account -> orders[account.id]?.let { account.copy(sortOrder = it) } }
    }

    /** Where a category goes: under [parentId] (`null` — top level) at [index] among the other children there. */
    data class Place(val parentId: String?, val index: Int)

    /**
     * Categories to save after putting [movedId] at [place]. [shown] — the live categories of one type in display order
     * (parents before their children, as on screen). Empty — the move is not allowed or changes nothing: a category with
     * subcategories cannot go inside another one, a parent must be a top-level category of the list, and
     * «Корректировка баланса» stays where it is.
     */
    fun moveCategory(shown: List<Category>, movedId: String, place: Place, manual: Boolean): List<Category> {
        val byId = shown.associateBy { it.id }
        val moved = byId[movedId] ?: return emptyList()
        if (!canPlace(shown, moved, place.parentId)) return emptyList()
        fun parentOf(c: Category) = c.parentId?.takeIf { it in byId }
        val siblings = shown.filter { parentOf(it) == place.parentId && it.id != movedId && !SystemCategories.isAdjustment(it.id) }
        val index = place.index.coerceIn(0, siblings.size)
        val sameParent = parentOf(moved) == place.parentId
        if (sameParent && index == indexIn(shown, moved, siblings)) return emptyList()
        val target = siblings.toMutableList().apply { add(index, moved) }
        val orders = if (manual) {
            val items = (siblings + moved).map { Reorder.Item(it.id, it.sortOrder) }
            Reorder.move(items, items.lastIndex, index)
        } else {
            // First manual move: every group of the list keeps its place on screen.
            val others = shown.filter { it.id != movedId && !SystemCategories.isAdjustment(it.id) && parentOf(it) != place.parentId }
                .groupBy { parentOf(it) }.values
            (others + listOf(target)).flatMap { group -> group.mapIndexed { i, c -> c.id to (i + 1) * Reorder.STEP } }.toMap()
        }
        return shown.mapNotNull { category ->
            val order = orders[category.id]
            val parent = if (category.id == movedId) place.parentId else category.parentId
            when {
                category.id == movedId && (order != null || !sameParent) -> category.copy(sortOrder = order ?: category.sortOrder, parentId = parent)
                order != null && order != category.sortOrder -> category.copy(sortOrder = order)
                else -> null
            }
        }
    }

    /** Can [moved] go under [parentId]? */
    fun canPlace(shown: List<Category>, moved: Category, parentId: String?): Boolean {
        if (SystemCategories.isAdjustment(moved.id)) return false
        if (parentId == null) return true
        val parent = shown.firstOrNull { it.id == parentId } ?: return false
        val ids = shown.map { it.id }.toSet()
        val parentIsTop = parent.parentId == null || parent.parentId !in ids
        val hasChildren = shown.any { it.parentId == moved.id }
        return parentId != moved.id && parentIsTop && !hasChildren && parent.type == moved.type && !SystemCategories.isAdjustment(parentId)
    }

    /** The index [moved] has now among [siblings] (its current neighbours on screen). */
    private fun indexIn(shown: List<Category>, moved: Category, siblings: List<Category>): Int {
        val at = shown.indexOf(moved)
        return siblings.count { shown.indexOf(it) < at }
    }

    private fun move(items: List<Reorder.Item>, from: Int, to: Int, manual: Boolean): Map<String, Int> {
        if (manual) return Reorder.move(items, from, to)
        if (from == to) return emptyMap()
        val list = items.toMutableList().apply { add(to, removeAt(from)) }
        return list.mapIndexedNotNull { i, item -> (item.id to (i + 1) * Reorder.STEP).takeIf { item.order != it.second } }.toMap()
    }
}

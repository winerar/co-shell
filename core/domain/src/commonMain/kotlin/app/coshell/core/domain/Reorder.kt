package app.coshell.core.domain

/**
 * Manual order by dragging (#338, owner 2026-11-17, variant А): accounts, and categories inside a folder.
 *
 * The order is shared by the family and synced row by row (last write wins per row), so a move should touch as few
 * rows as possible: the moved item takes a number between its new neighbours, and the whole list is renumbered with
 * gaps of [STEP] only when there is no room left. Two people moving different items at once then rarely clash.
 */
object Reorder {

    const val STEP = 1024

    /** An item of the list as shown: its id and current sort number. */
    data class Item(val id: String, val order: Int)

    /**
     * New sort numbers after moving the item at [from] to [to] (indices in [shown], which is in display order).
     * Returns only the changed ids; an empty map — nothing to save.
     */
    fun move(shown: List<Item>, from: Int, to: Int): Map<String, Int> {
        require(from in shown.indices && to in shown.indices) { "Index out of range: $from → $to of ${shown.size}" }
        if (from == to) return emptyMap()
        val list = shown.toMutableList()
        val moved = list.removeAt(from)
        list.add(to, moved)
        val before = list.getOrNull(to - 1)?.order
        val after = list.getOrNull(to + 1)?.order
        val slot = between(before, after)
        val ordered = list.zipWithNext().all { (a, b) -> a.order < b.order || a === moved || b === moved }
        if (slot != null && ordered) return mapOf(moved.id to slot)
        return renumber(list)
    }

    /** Sort number for an item put at the end (a new account, a category moved into another folder). */
    fun last(shown: List<Item>): Int = (shown.maxOfOrNull { it.order } ?: 0) + STEP

    /** A free number strictly between [before] and [after]; `null` — no room. */
    private fun between(before: Int?, after: Int?): Int? = when {
        before == null && after == null -> 0
        before == null -> after!!.toLong().minus(STEP).takeIf { it >= Int.MIN_VALUE }?.toInt()
        after == null -> before.toLong().plus(STEP).takeIf { it <= Int.MAX_VALUE }?.toInt()
        after.toLong() - before.toLong() >= 2 -> ((before.toLong() + after.toLong()) / 2).toInt()
        else -> null
    }

    private fun renumber(list: List<Item>): Map<String, Int> =
        list.mapIndexedNotNull { index, item ->
            val order = (index + 1) * STEP
            (item.id to order).takeIf { item.order != order }
        }.toMap()
}

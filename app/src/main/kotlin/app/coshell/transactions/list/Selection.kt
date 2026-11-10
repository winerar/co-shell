package app.coshell.transactions.list

/**
 * Rows chosen in the list's selection mode (19В, issue #112): a long press starts it with that row, taps add
 * or remove rows, the mode ends when nothing is left. Pure, so the rules are tested without Compose.
 */
data class Selection(val ids: Set<String> = emptySet()) {
    val active: Boolean get() = ids.isNotEmpty()
    val count: Int get() = ids.size

    /** «Дублировать» works on exactly one row: a copy opens in the editor. */
    val single: String? get() = ids.singleOrNull()

    operator fun contains(id: String): Boolean = id in ids

    /** Long press: starts the mode with [id]; while selecting it toggles like a tap. */
    fun longPress(id: String): Selection = if (active) toggle(id) else Selection(setOf(id))

    fun toggle(id: String): Selection = Selection(if (id in ids) ids - id else ids + id)

    /** Rows that left the list (deleted on the other phone, filtered out) drop out of the selection. */
    fun keepOnly(visible: Set<String>): Selection = if (ids.all { it in visible }) this else Selection(ids intersect visible)
}

package app.coshell.core.domain.newmark

/**
 * The «новое» marks on the way to a new place (owner 2026-11-28): a place that holds others («Настройки», «Внешний вид») wears
 * the mark while anything inside it does, so the pill leads from «Ещё» to the very row. [inside] — place → the places in it;
 * only the innermost places have marks of their own ([NewMarks]), the ones on the way are worked out here.
 */
object NewMarkTree {
    /** [current] (marks of the places themselves) plus every place with a marked one somewhere inside it. */
    fun marked(current: Set<String>, inside: Map<String, List<String>>): Set<String> {
        val out = LinkedHashSet<String>()
        fun has(place: String, seen: Set<String>): Boolean {
            val children = inside[place] ?: return place in current
            if (place in seen) return false
            return children.map { has(it, seen + place) }.any { it }
        }
        (inside.keys + current).forEach { if (has(it, emptySet())) out += it }
        return out
    }

    /** The places that only lead to others: they never wear a mark of their own and opening them takes no mark away. */
    fun waypoints(inside: Map<String, List<String>>): Set<String> = inside.keys
}

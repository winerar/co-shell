package app.coshell.core.domain.tags

import app.coshell.core.model.Tag
import app.coshell.core.model.TagIds

/** Orders for the tag pickers (#129, boards `Fb129A`, `Fb129Pick`, `Fb129All`). */
object TagRanking {
    /** The [limit] tags used most recently (unused ones never); ties — the more used first. */
    fun recent(tags: List<Tag>, usage: Map<String, TagUsage>, limit: Int): List<Tag> =
        tags.filter { it.id in usage }
            .sortedWith(compareByDescending<Tag> { usage.getValue(it.id).lastUsed }.thenByDescending { usage.getValue(it.id).count })
            .take(limit)

    /** Most used first, then by name — «Ещё → Теги». */
    fun frequent(tags: List<Tag>, usage: Map<String, TagUsage>): List<Tag> =
        tags.sortedWith(compareByDescending<Tag> { usage[it.id]?.count ?: 0 }.thenBy { it.name.lowercase() })

    /** Tags whose name contains [query] (case and extra spaces ignored), names starting with it first. */
    fun search(tags: List<Tag>, query: String): List<Tag> {
        val q = TagIds.of(query)
        if (q.isEmpty()) return tags
        return tags.map { it to TagIds.of(it.name) }
            .filter { (_, key) -> q in key }
            .sortedBy { (_, key) -> if (key.startsWith(q)) 0 else 1 }
            .map { it.first }
    }
}

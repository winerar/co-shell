package app.coshell.templates.list

import app.coshell.core.domain.templates.frequent
import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateCatalog
import app.coshell.templates.TemplateItem

/** Templates of one type, sorted by category (А→Я), then by name. */
data class TemplateSection(val type: TransactionType, val items: List<TemplateItem>)

data class TemplatesUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    /** `null` — «Все». */
    val filter: TransactionType? = null,
    /** All templates, whatever the search and the filter say — «Найти шаблон · 32». */
    val total: Int = 0,
    /** How many templates each type has (types without templates are absent) — the filter chips. */
    val counts: Map<TransactionType, Int> = emptyMap(),
    /** «Часто»: the most used on this phone; hidden while searching. */
    val frequent: List<TemplateItem> = emptyList(),
    val sections: List<TemplateSection> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && total == 0
    val nothingFound: Boolean get() = !isLoading && total > 0 && sections.isEmpty()
}

internal const val FREQUENT_LIMIT = 3

/** Pure: what «Все шаблоны» shows for the [snapshot], the search [query] and the type [filter]. */
internal fun templateList(snapshot: TemplateCatalog.Snapshot, query: String, filter: TransactionType?): TemplatesUiState {
    val needle = query.trim()
    val counts = snapshot.items.groupingBy { it.type }.eachCount()
    // A filter whose last template was deleted would show an empty screen with no chip to leave it.
    val type = filter?.takeIf { it in counts }
    val shown = snapshot.items.filter { (type == null || it.type == type) && it.matches(needle) }
    return TemplatesUiState(
        query = query,
        filter = type,
        total = snapshot.items.size,
        counts = counts,
        frequent = if (needle.isEmpty()) {
            snapshot.items.filter { type == null || it.type == type }.frequent(snapshot.counts, FREQUENT_LIMIT) { it.id }
        } else {
            emptyList()
        },
        sections = TransactionType.entries.mapNotNull { sectionType ->
            shown.filter { it.type == sectionType }
                .sortedWith(compareBy({ it.categoryName.lowercase() }, { it.name.lowercase() }))
                .takeIf { it.isNotEmpty() }
                ?.let { TemplateSection(sectionType, it) }
        },
    )
}

/** By name, category or account — «свет», «подписки», «galicia». */
private fun TemplateItem.matches(needle: String): Boolean = needle.isEmpty() ||
    listOf(name, categoryName, accountName, toAccountName).any { it.contains(needle, ignoreCase = true) }

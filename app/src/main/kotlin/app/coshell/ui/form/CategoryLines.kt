package app.coshell.ui.form

/**
 * A category to pick (#439): [parent] — the name of its parent category, [parentId] — its id; both `null` — it has none.
 */
internal data class CategoryLine(
    val id: String,
    val name: String,
    val parent: String?,
    val icon: String?,
    val color: Long?,
    val parentId: String? = null,
)

/**
 * Lines of every category list sheet, laid out as the «Категории» screen: top-level categories in their order, one with
 * subcategories subtitled [parentSubtitle] and followed by its subcategories, indented with a smaller badge («Дети» →
 * «Дети» is a category and its subcategory inside, not a double). Subcategories whose parent cannot be picked keep the
 * parent's name as a caption, after the rest. Search still finds a subcategory by its parent's name.
 */
internal fun categoryLines(lines: List<CategoryLine>, parentSubtitle: String): List<ChoiceOption<String>> {
    val byId = lines.associateBy { it.id }
    val children = lines.filter { it.parentId != null }.groupBy { it.parentId }
    fun CategoryLine.option(subtitle: String? = null, indent: Boolean = false, group: String? = null) =
        ChoiceOption(id, name, subtitle, group, icon, color, hasBadge = true, indent = indent, parent = parent)
    val tree = lines.filter { it.parentId == null }.flatMap { top ->
        val kids = children[top.id].orEmpty()
        listOf(top.option(subtitle = parentSubtitle.takeIf { kids.isNotEmpty() })) + kids.map { it.option(indent = true) }
    }
    val orphans = lines.filter { it.parentId != null && it.parentId !in byId }
        .map { it.option(group = it.parent ?: "") }
    return tree + orphans
}

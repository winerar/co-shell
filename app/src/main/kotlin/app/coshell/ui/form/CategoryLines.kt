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
 * Lines of every category list sheet (#439): each category with subcategories is a group under its own name — itself
 * first (when it can be picked, subtitled [parentSubtitle]), then its subcategories, even one and even of the same name
 * («Дети» → «Дети» is a category and its subcategory, not a double). Categories without subcategories and without a
 * parent go under [noGroup], after the groups. Order inside a run is kept.
 */
internal fun categoryLines(lines: List<CategoryLine>, noGroup: String, parentSubtitle: String): List<ChoiceOption<String>> {
    fun CategoryLine.option(group: String, subtitle: String?) = ChoiceOption(id, name, subtitle, group, icon, color, hasBadge = true)
    val byId = lines.associateBy { it.id }
    val parents = lines.mapNotNull { line -> line.parentId?.let { it to (byId[it]?.name ?: line.parent ?: "") } }.distinct()
    val grouped = parents.flatMap { (parentId, name) ->
        listOfNotNull(byId[parentId]?.option(name, parentSubtitle)) +
            lines.filter { it.parentId == parentId }.map { it.option(name, null) }
    }
    val placed = grouped.map { it.value }.toSet()
    return grouped + lines.filter { it.id !in placed }.map { it.option(noGroup, null) }
}

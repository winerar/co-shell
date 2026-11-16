package app.coshell.ui.form

/** A category to pick (#439): [parent] — the name of its folder, `null` — it has none. */
internal data class CategoryLine(val id: String, val name: String, val parent: String?, val icon: String?, val color: Long?)

/**
 * Lines of every category list sheet (#439, poll А): a folder with two or more categories gets its own caption, all others —
 * those without a folder or alone in it — go under [noGroup], after the folders. A folder of one is just that category:
 * no «Дети / Дети» double; its name goes to the subtitle only when it differs. Order inside a run is kept.
 */
internal fun categoryLines(lines: List<CategoryLine>, noGroup: String): List<ChoiceOption<String>> {
    val crowded = lines.mapNotNull { it.parent }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    fun CategoryLine.option(group: String, subtitle: String?) = ChoiceOption(id, name, subtitle, group, icon, color, hasBadge = true)
    val grouped = lines.filter { it.parent in crowded }.sortedBy { line -> crowded.indexOf(line.parent) }
        .map { it.option(it.parent!!, null) }
    val rest = lines.filter { it.parent !in crowded }.map { it.option(noGroup, it.parent?.takeUnless { p -> p == it.name }) }
    return grouped + rest
}

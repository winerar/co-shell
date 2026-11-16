package app.coshell.transactions.edit

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.TransactionType
import app.coshell.core.model.buildCategoryTree
import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.transactions.edit.TransactionEditUiState.CategoryItem
import app.coshell.ui.form.ChoiceOption

/**
 * Recomputes the category choices for the current type from [all] categories and drops a selection
 * that no longer fits (e.g. an expense category after switching to income). Transfers have none.
 * Categories from [recent] operations (newest first) come first — the grid of the entry sheet.
 */
internal fun TransactionEditUiState.withCategoryOptions(
    all: List<Category>,
    recent: List<String> = emptyList(),
): TransactionEditUiState {
    val categoryType = when (type) {
        TransactionType.EXPENSE -> CategoryType.EXPENSE
        TransactionType.INCOME -> CategoryType.INCOME
        TransactionType.TRANSFER -> return copy(categories = emptyList(), frequentCategories = emptyList(), categoryId = null, openFolderId = null)
    }
    val ofType = all.filter { it.type == categoryType }
    // Recently used first (a folder counts by its subcategories too), the rest keep their order.
    val rank = recent.withIndex().associate { (index, id) -> id to index }
    val tree = buildCategoryTree(ofType).sortedBy { node ->
        (listOf(node.category.id) + node.children.map { it.id }).mapNotNull { rank[it] }.minOrNull() ?: Int.MAX_VALUE
    }
    val selectedId = ofType.firstOrNull { it.id == categoryId }?.id
    val openFolder = tree.firstOrNull { node ->
        node.isFolder && (node.category.id == selectedId || node.children.any { it.id == selectedId })
    }
    val groups = tree.map { node -> CategoryGroup(node.category.toItem(), node.children.map { it.toItem() }) }
    return copy(
        categories = groups,
        frequentCategories = frequentCategories(groups, recent, selectedId),
        categoryId = selectedId,
        openFolderId = openFolder?.category?.id,
    )
}

/**
 * «Категория · частые» of the entry sheet (#290, board `Fb290A`): up to [limit] categories one tap away — the ones
 * of [recent] operations first (a folder too, if operations were saved on it), then the rest of the subcategories
 * and folderless categories in tree order. The [selectedId] category is always among them: picked from «Все», it
 * takes the last place. Everything fits in `limit + 1` places — then all are shown and «Все» is not needed.
 */
internal fun frequentCategories(
    groups: List<CategoryGroup>,
    recent: List<String>,
    selectedId: String?,
    limit: Int = FREQUENT_CATEGORIES,
): List<CategoryItem> {
    val byId = groups.flatMap { listOf(it.item) + it.children }.associateBy { it.id }
    val leaves = groups.flatMap { if (it.isFolder) it.children else listOf(it.item) }
    val ordered = (recent.mapNotNull { byId[it] } + leaves).distinctBy { it.id }
    if (ordered.size <= limit + 1) return ordered
    val top = ordered.take(limit)
    val selected = selectedId?.let { byId[it] } ?: return top
    return if (top.any { it.id == selected.id }) top else top.take(limit - 1) + selected
}

/** How many categories the sheet can choose from — the number on «Все 24» (folders are not counted). */
internal val List<CategoryGroup>.leafCount: Int get() = sumOf { if (it.isFolder) it.children.size else 1 }

/** Places of the frequent grid; the eighth one is «Все N» (two rows of four). */
internal const val FREQUENT_CATEGORIES = 7

private fun Category.toItem() = CategoryItem(id, name, icon, color)

/**
 * Lines of the category list sheet (#439, poll А): each folder with two or more subcategories under its own caption, then
 * every category that has no folder — or is alone in it — under [noGroup]. A folder of one is just that category: no
 * «Дети / Дети» double; its name goes to the subtitle only when it differs.
 */
internal fun categoryChoiceOptions(groups: List<CategoryGroup>, noGroup: String): List<ChoiceOption<String>> {
    fun CategoryItem.option(group: String?, subtitle: String? = null) = ChoiceOption(id, name, subtitle, group, icon, color, hasBadge = true)
    val folders = groups.filter { it.children.size > 1 }.flatMap { g -> g.children.map { it.option(g.item.name) } }
    val loose = groups.filter { it.children.size <= 1 }.map { g ->
        val only = g.children.firstOrNull()
        if (only == null) g.item.option(noGroup) else only.option(noGroup, g.item.name.takeUnless { it == only.name })
    }
    return folders + loose
}

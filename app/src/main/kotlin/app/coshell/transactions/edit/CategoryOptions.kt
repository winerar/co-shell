package app.coshell.transactions.edit

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.TransactionType
import app.coshell.core.model.buildCategoryTree
import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.transactions.edit.TransactionEditUiState.CategoryItem
import app.coshell.ui.form.CategoryLine
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.form.categoryLines

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
    // Parent categories can be picked too (#439) — after every subcategory and plain category.
    val leaves = groups.flatMap { if (it.isFolder) it.children else listOf(it.item) } + groups.filter { it.isFolder }.map { it.item }
    val ordered = (recent.mapNotNull { byId[it] } + leaves).distinctBy { it.id }
    if (ordered.size <= limit + 1) return ordered
    val top = ordered.take(limit)
    val selected = selectedId?.let { byId[it] } ?: return top
    return if (top.any { it.id == selected.id }) top else top.take(limit - 1) + selected
}

/** How many categories the sheet can choose from — the number on «Все 24» (a category with subcategories counts too: it can be picked). */
internal val List<CategoryGroup>.leafCount: Int get() = sumOf { 1 + it.children.size }

/** Places of the frequent grid; the eighth one is «Все N» (two rows of four). */
internal const val FREQUENT_CATEGORIES = 7

private fun Category.toItem() = CategoryItem(id, name, icon, color)

/** Lines of the «Все категории» sheet of the entry sheet (#439): every category, a parent one with its subcategories, as [categoryLines]. */
internal fun categoryChoiceOptions(groups: List<CategoryGroup>, noGroup: String, parentSubtitle: String): List<ChoiceOption<String>> =
    categoryLines(
        groups.flatMap { g -> listOf(g.item.line(null)) + g.children.map { it.line(g.item) } },
        noGroup,
        parentSubtitle,
    )

private fun CategoryItem.line(parent: CategoryItem?) = CategoryLine(id, name, parent?.name, icon, color, parent?.id)

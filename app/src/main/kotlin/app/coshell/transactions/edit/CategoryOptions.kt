package app.coshell.transactions.edit

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.TransactionType
import app.coshell.core.model.buildCategoryTree
import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.transactions.edit.TransactionEditUiState.CategoryItem

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
        TransactionType.TRANSFER -> return copy(categories = emptyList(), categoryId = null, openFolderId = null)
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
    return copy(
        categories = tree.map { node -> CategoryGroup(node.category.toItem(), node.children.map { it.toItem() }) },
        categoryId = selectedId,
        openFolderId = openFolder?.category?.id,
    )
}

private fun Category.toItem() = CategoryItem(id, name, icon, color)

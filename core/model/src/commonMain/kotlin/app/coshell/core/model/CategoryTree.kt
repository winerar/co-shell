package app.coshell.core.model

/** A top-level category and its subcategories; a node with children is shown as a folder. */
data class CategoryNode(val category: Category, val children: List<Category>) {
    val isFolder: Boolean get() = children.isNotEmpty()
}

/**
 * Groups [categories] into one level of folders, sorted by [order]; `null` keeps the order of the list (the repositories
 * already sort it — alphabetically or in the family's manual order, #338).
 * A subcategory whose folder is not in the list (archived or deleted, e.g. on another phone)
 * is shown at the top level so it never disappears.
 */
fun buildCategoryTree(categories: List<Category>, order: Comparator<Category>? = null): List<CategoryNode> {
    val sorted = order?.let { categories.sortedWith(it) } ?: categories
    val ids = sorted.map { it.id }.toSet()
    val children = sorted.filter { it.parentId != null && it.parentId in ids }.groupBy { it.parentId }
    return sorted.filter { it.parentId == null || it.parentId !in ids }
        .map { CategoryNode(it, children[it.id].orEmpty()) }
}

/** Categories alphabetically ([nameOrder], #338); «Корректировка баланса» always last among its siblings. */
val CATEGORY_ORDER: Comparator<Category> =
    compareBy<Category> { SystemCategories.isAdjustment(it.id) }.then(nameOrder { it.name }).thenBy { it.id }

/** Categories after a manual order (#338): by `sortOrder`, ties alphabetically; «Корректировка баланса» still last. */
val MANUAL_CATEGORY_ORDER: Comparator<Category> =
    compareBy<Category> { SystemCategories.isAdjustment(it.id) }.thenBy { it.sortOrder }.then(nameOrder { it.name }).thenBy { it.id }

/** The category order of a list: [MANUAL_CATEGORY_ORDER] once [manual], else [CATEGORY_ORDER]. */
fun categoryOrder(manual: Boolean): Comparator<Category> = if (manual) MANUAL_CATEGORY_ORDER else CATEGORY_ORDER

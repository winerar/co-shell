package app.coshell.core.model

/** A top-level category and its subcategories; a node with children is shown as a folder. */
data class CategoryNode(val category: Category, val children: List<Category>) {
    val isFolder: Boolean get() = children.isNotEmpty()
}

/**
 * Groups [categories] into one level of folders, ordered by `sortOrder`, then name.
 * A subcategory whose folder is not in the list (archived or deleted, e.g. on another phone)
 * is shown at the top level so it never disappears.
 */
fun buildCategoryTree(categories: List<Category>): List<CategoryNode> {
    val ids = categories.map { it.id }.toSet()
    val children = categories.filter { it.parentId != null && it.parentId in ids }.groupBy { it.parentId }
    return categories.filter { it.parentId == null || it.parentId !in ids }
        .sortedWith(CATEGORY_ORDER)
        .map { CategoryNode(it, children[it.id].orEmpty().sortedWith(CATEGORY_ORDER)) }
}

val CATEGORY_ORDER: Comparator<Category> = compareBy<Category>({ it.sortOrder }, { it.name })

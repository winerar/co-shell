package app.coshell.categories.list

import app.coshell.core.model.CategoryType

data class CategoriesUiState(
    val isLoading: Boolean = false,
    val type: CategoryType = CategoryType.EXPENSE,
    /** Top-level categories of [type]; those with children are shown as folders. */
    val groups: List<Group> = emptyList(),
    val archived: List<Item> = emptyList(),
    val showArchived: Boolean = false,
) {
    data class Group(val item: Item, val children: List<Item>, val expanded: Boolean) {
        val isFolder: Boolean get() = children.isNotEmpty()
    }

    data class Item(val id: String, val name: String, val color: Long?, val icon: String? = null)
}

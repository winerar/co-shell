package app.coshell.categories.edit

import app.coshell.core.domain.SaveCategoryUseCase
import app.coshell.core.model.CategoryType

data class CategoryEditUiState(
    val isLoading: Boolean = false,
    val isNew: Boolean = true,
    /** Opened via "new folder": the folder picker is hidden. */
    val asFolder: Boolean = false,
    val name: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val typeLocked: Boolean = false,
    /** Folder this category is in, `null` for the top level. */
    val parentId: String? = null,
    /** Folders it can be put into; empty while it has subcategories itself. */
    val folders: List<Option> = emptyList(),
    /** Top-level categories that can be put into this one (it then becomes a folder). */
    val candidates: List<Option> = emptyList(),
    val childIds: Set<String> = emptySet(),
    val archived: Boolean = false,
    val nameError: Boolean = false,
    val saveError: SaveCategoryUseCase.Reason? = null,
    val deleteBlocked: Boolean = false,
    val isDone: Boolean = false,
) {
    data class Option(val id: String, val name: String)

    /** Subcategories can only be chosen for a category that is not inside a folder. */
    val canHaveChildren: Boolean get() = parentId == null
    val canMoveToFolder: Boolean get() = !asFolder && childIds.isEmpty()
}

package app.coshell.core.domain

import app.coshell.core.model.Category
import kotlinx.coroutines.flow.first

// Categories form at most two levels: a top-level category may act as a folder for
// subcategories ([Category.parentId]). A folder is still a normal category and can be picked
// for a transaction itself.

/** Creates or updates a category, keeping the two-level folder structure valid. */
class SaveCategoryUseCase(
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
) {
    sealed interface Result {
        data object Saved : Result
        data class Invalid(val reason: Reason) : Result
    }

    enum class Reason {
        /** The chosen folder does not exist (or is archived/deleted). */
        PARENT_NOT_FOUND,
        /** The chosen folder is itself inside a folder: only one level of nesting. */
        PARENT_NOT_TOP_LEVEL,
        PARENT_TYPE_MISMATCH,
        /** The category is a folder with subcategories, so it cannot go into another folder. */
        HAS_SUBCATEGORIES,
        /** Expense/income cannot change once the category is used or has subcategories. */
        TYPE_LOCKED,
    }

    suspend operator fun invoke(category: Category): Result {
        val all = categories.observeCategories(includeArchived = true).first()
        val existing = all.firstOrNull { it.id == category.id }
        val children = all.filter { it.parentId == category.id }

        category.parentId?.let { parentId ->
            val parent = all.firstOrNull { it.id == parentId && !it.archived }
            when {
                parent == null || parentId == category.id -> return invalid(Reason.PARENT_NOT_FOUND)
                parent.parentId != null -> return invalid(Reason.PARENT_NOT_TOP_LEVEL)
                parent.type != category.type -> return invalid(Reason.PARENT_TYPE_MISMATCH)
                children.isNotEmpty() -> return invalid(Reason.HAS_SUBCATEGORIES)
            }
        }
        if (existing != null && existing.type != category.type && isTypeLocked(category.id)) {
            return invalid(Reason.TYPE_LOCKED)
        }

        val toSave = if (existing == null || existing.parentId != category.parentId) {
            // New or moved: goes to the end of its new folder (or of the top level).
            val siblings = all.filter { it.parentId == category.parentId && it.id != category.id }
            category.copy(sortOrder = siblings.maxOfOrNull { it.sortOrder + 1 } ?: 0)
        } else {
            category
        }
        categories.saveCategory(toSave)
        return Result.Saved
    }

    suspend fun isTypeLocked(categoryId: String): Boolean =
        transactions.countForCategory(categoryId) > 0 ||
            categories.observeCategories(includeArchived = true).first().any { it.parentId == categoryId }

    private fun invalid(reason: Reason) = Result.Invalid(reason)
}

/**
 * Archives or restores a category. Archiving a folder moves its subcategories to the top level,
 * so they stay visible and usable.
 */
class ArchiveCategoryUseCase(private val categories: CategoryRepository) {
    suspend operator fun invoke(categoryId: String, archived: Boolean) {
        val category = categories.getCategory(categoryId) ?: return
        val lifted = if (archived) liftChildren(categories, categoryId) else emptyList()
        categories.saveCategories(lifted + category.copy(archived = archived))
    }
}

/** Subcategories of [folderId] moved to the top level, ready to be saved. */
internal suspend fun liftChildren(categories: CategoryRepository, folderId: String): List<Category> =
    categories.observeCategories(includeArchived = true).first()
        .filter { it.parentId == folderId }
        .map { it.copy(parentId = null) }

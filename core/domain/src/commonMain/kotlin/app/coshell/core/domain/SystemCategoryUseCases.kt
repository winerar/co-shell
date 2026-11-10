package app.coshell.core.domain

import app.coshell.core.model.CategoryType
import app.coshell.core.model.SystemCategories

/**
 * Makes sure both «Корректировка баланса» categories exist and are live: creates a missing (or
 * deleted) one, restores an archived one keeping the user's name and look. Returns their ids by
 * direction — ready for `WalletImportDecisions.adjustmentCategories`.
 */
class EnsureAdjustmentCategoriesUseCase(private val categories: CategoryRepository) {
    suspend operator fun invoke(): Map<CategoryType, String> {
        val toSave = CategoryType.entries.mapNotNull { type ->
            val existing = categories.getCategory(SystemCategories.adjustmentId(type))
            when {
                existing == null -> SystemCategories.adjustment(type)
                existing.archived || existing.type != type -> existing.copy(archived = false, type = type)
                else -> null
            }
        }
        if (toSave.isNotEmpty()) categories.saveCategories(toSave)
        return CategoryType.entries.associateWith(SystemCategories::adjustmentId)
    }
}

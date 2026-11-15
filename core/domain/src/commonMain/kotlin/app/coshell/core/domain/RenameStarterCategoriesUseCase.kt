package app.coshell.core.domain

import app.coshell.core.model.SystemCategories
import kotlinx.coroutines.flow.first

/**
 * Renames the categories the app created itself from the language [from] to [to] — when the language is picked
 * on the first launch (#352): the starter set is already there, named in the phone's language. Only names nobody
 * changed are touched (a category named exactly as the app named it); the user's own names stay. Returns the count.
 */
class RenameStarterCategoriesUseCase(private val categories: CategoryRepository) {
    suspend operator fun invoke(from: CategoryNames, to: CategoryNames): Int {
        val renamed = categories.observeCategories(includeArchived = true).first().mapNotNull { category ->
            val old: String?
            val new: String?
            if (SystemCategories.isAdjustment(category.id)) {
                old = from.adjustment()
                new = to.adjustment()
            } else {
                val icon = category.icon ?: return@mapNotNull null
                old = DefaultCategories.name(category.type, icon, from)
                new = DefaultCategories.name(category.type, icon, to)
            }
            if (old == null || new == null || old == new || category.name != old) null else category.copy(name = new)
        }
        if (renamed.isNotEmpty()) categories.saveCategories(renamed)
        return renamed.size
    }

    private fun CategoryNames.adjustment(): String =
        name(CategoryNames.ADJUSTMENT)?.takeIf { it.isNotBlank() } ?: SystemCategories.ADJUSTMENT_NAME
}

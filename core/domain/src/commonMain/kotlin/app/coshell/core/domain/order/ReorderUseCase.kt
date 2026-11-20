package app.coshell.core.domain.order

import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.settings.ManualOrderSettings
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import kotlinx.coroutines.flow.first

/** Saves a drag of «Порядок» (#338): the rows [ManualOrder] changed, then switches the list to its manual order. */
class ReorderUseCase(
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val settings: ManualOrderSettings,
) {
    /** [shown] — active accounts in display order. */
    suspend fun moveAccount(shown: List<Account>, from: Int, to: Int) {
        val manual = settings.manualAccounts.first()
        val changed = ManualOrder.moveAccount(shown, from, to, manual)
        if (changed.isEmpty()) return
        accounts.saveAccounts(changed)
        if (!manual) settings.setManualAccounts()
    }

    /**
     * What a category drag changes ([ManualOrder.moveCategory]); empty — not allowed or nothing to do. The screen shows
     * it at once, then calls [saveCategories].
     */
    suspend fun planCategory(shown: List<Category>, movedId: String, place: ManualOrder.Place): List<Category> =
        ManualOrder.moveCategory(shown, movedId, place, settings.manualCategories.first())

    /** Saves a [planCategory] result and switches the categories to their manual order. */
    suspend fun saveCategories(changed: List<Category>) {
        if (changed.isEmpty()) return
        categories.saveCategories(changed)
        if (!settings.manualCategories.first()) settings.setManualCategories()
    }
}

package app.coshell.transactions.edit.tip

import app.coshell.core.domain.SaveCategoryUseCase
import app.coshell.core.domain.quickentry.TipPrefs
import app.coshell.core.domain.quickentry.TipSettings
import app.coshell.core.domain.quickentry.Tips
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.IdGenerator
import javax.inject.Inject

/**
 * Tips for the entry sheet (#322): what is remembered between operations, the category «Чаевые» and the tip operation.
 * Keeps [app.coshell.transactions.edit.TransactionEditViewModel] thin, like `EntryTags`.
 */
class EntryTip @Inject constructor(
    private val settings: TipSettings,
    private val saveCategory: SaveCategoryUseCase,
    private val ids: IdGenerator,
) {
    /** The sheet for [ctx]'s purchase: the last tip account (else the purchase's), the remembered category (else asked). */
    internal suspend fun open(ctx: TipContext, categories: List<Category>): TipUi {
        val prefs = settings.load()
        val account = prefs.accountId?.takeIf { it in ctx.accountsById } ?: ctx.main.accountId
        val category = prefs.categoryId?.takeIf { it == TipPrefs.SAME_AS_PURCHASE || categories.any { c -> c.id == it && !c.archived } }
        return TipUi(accountId = account, categoryId = category, asking = category == null).refreshed(ctx)
    }

    /** An existing expense category named [name] (case ignored), else a new top-level one; `null` if it could not be saved. */
    suspend fun categoryNamed(name: String, categories: List<Category>): String? {
        categories.firstOrNull { it.type == CategoryType.EXPENSE && !it.archived && it.name.trim().equals(name.trim(), ignoreCase = true) }
            ?.let { return it.id }
        val created = Category(ids.newId(), name.trim(), CategoryType.EXPENSE, icon = ICON, color = COLOR)
        return created.id.takeIf { saveCategory(created) == SaveCategoryUseCase.Result.Saved }
    }

    /** The tip operation for [main], or the reason it cannot be made ([Tips.Result.Invalid]). */
    internal fun transaction(ui: TipUi, ctx: TipContext, note: String): Tips.Result {
        val account = ui.accountId?.let { ctx.accountsById[it] } ?: return Tips.Result.Invalid(Tips.Reason.TIP_NOT_POSITIVE)
        val tip = ui.tipMoney(ctx.main) ?: return Tips.Result.Invalid(Tips.Reason.TIP_NOT_POSITIVE)
        val result = Tips.transaction(ctx.main, tip, account, ids.newId(), note) { money, to -> convert(ctx, money, to) }
        val category = ui.categoryId?.takeIf { it != TipPrefs.SAME_AS_PURCHASE } ?: return result
        return if (result is Tips.Result.Ok) Tips.Result.Ok(result.transaction.copy(categoryId = category)) else result
    }

    /** After the tip is saved: the next sheet starts with this account and category. */
    suspend fun remember(ui: TipUi) = settings.save(TipPrefs(categoryId = ui.categoryId, accountId = ui.accountId))

    private companion object {
        const val ICON = "gift"
        const val COLOR = 0xFFFF8FA3
    }
}


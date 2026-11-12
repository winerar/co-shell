package app.coshell.budgets.edit

import app.coshell.budgets.edit.BudgetEditUiState.Choice
import app.coshell.budgets.edit.BudgetEditUiState.Inside
import app.coshell.core.domain.budgets.forCategory
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.MoneyParser
import app.coshell.core.model.SystemCategories
import kotlinx.datetime.YearMonth

// Pure parts of the limit editor: what can carry a limit, what is inside a folder, the typed amount.

/** Live expense folders (with at least one live subcategory) and plain categories, А→Я; system ones are left out. */
internal fun budgetChoices(categories: List<Category>): Pair<List<Choice>, List<Choice>> {
    val live = categories.filter { !it.archived && it.type == CategoryType.EXPENSE && SystemCategories.adjustmentType(it.id) == null }
    val byId = live.associateBy { it.id }
    val parents = live.mapNotNull { it.parentId }.toSet()
    val folders = live.filter { it.id in parents }.map { Choice(it.id, it.name, it.icon, it.color) }
    val plain = live.filter { it.id !in parents }.map { category ->
        Choice(category.id, category.name, category.icon, category.color, parent = category.parentId?.let { byId[it]?.name })
    }
    return folders.sortedBy { it.name.lowercase() } to plain.sortedBy { it.label.lowercase() }
}

/** The live subcategories of [folderId] and the limit each has of its own in [month]. */
internal fun insideFolder(folderId: String, categories: List<Category>, budgets: List<Budget>, month: YearMonth, format: MoneyFormat): List<Inside> =
    categories.filter { it.parentId == folderId && !it.archived }
        .sortedBy { it.name.lowercase() }
        .map { Inside(it.id, it.name, it.icon, it.color, budgets.forCategory(it.id, month)?.limit?.let(format::format)) }

/** The limit typed in [currency]; `null` — empty, not a number or not above zero. */
internal fun parseLimit(amount: String, currency: String): Money? {
    val code = CurrencyCode.parseOrNull(currency) ?: return null
    return MoneyParser.parseOrNull(amount, code)?.takeIf { it.isPositive }
}

/** Currencies a limit can be in: the report currencies first, then those of the accounts and of the current limit. */
internal fun limitCurrencies(report: List<CurrencyCode>, accounts: List<CurrencyCode>, current: CurrencyCode?): List<String> =
    (report + accounts + listOfNotNull(current)).map { it.value }.distinct()

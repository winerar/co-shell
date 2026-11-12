package app.coshell.budgets.edit

import kotlinx.datetime.YearMonth

/** «Папка | Категория» of the limit editor (`BudEdit`). */
enum class BudgetKind { FOLDER, CATEGORY }

/** Limit editor (mockup `BudEdit`). [month] — the change applies from it on; earlier months keep their limit. */
data class BudgetEditUiState(
    val isLoading: Boolean = false,
    val isDone: Boolean = false,
    val month: YearMonth? = null,
    /** Opened from an envelope: what the limit is on is fixed. */
    val locked: Boolean = false,
    val kind: BudgetKind = BudgetKind.FOLDER,
    val folders: List<Choice> = emptyList(),
    val categories: List<Choice> = emptyList(),
    val categoryId: String? = null,
    val categoryError: Boolean = false,
    val amount: String = "",
    val amountError: Boolean = false,
    val currency: String = "",
    val currencies: List<String> = emptyList(),
    /** «В среднем за 3 месяца — 372 000 ARS» in [currency]; `null` — no history. */
    val average: String? = null,
    /** Subcategories of the chosen folder with their own limits (`BudEdit` «Внутри папки»). */
    val inside: List<Inside> = emptyList(),
    val rollover: Boolean = false,
    val note: String = "",
    /** The category already has a limit in [month]: «Убрать лимит» is offered. */
    val hasLimit: Boolean = false,
    /** The use case refused (archived on the other phone meanwhile). */
    val saveFailed: Boolean = false,
) {
    val choices: List<Choice> get() = if (kind == BudgetKind.FOLDER) folders else categories
    val chosen: Choice? get() = (folders + categories).firstOrNull { it.id == categoryId }

    /** A category or folder to put a limit on. [parent] — the folder of a subcategory. */
    data class Choice(val id: String, val name: String, val icon: String?, val color: Long?, val parent: String? = null) {
        val label: String get() = if (parent != null) "$parent / $name" else name
    }

    /** [limit] — formatted with the code; `null` — «без своего лимита». */
    data class Inside(val categoryId: String, val name: String, val icon: String?, val color: Long?, val limit: String?)
}

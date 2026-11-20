package app.coshell.budgets.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.budgets.edit.BudgetEditActions
import app.coshell.budgets.edit.BudgetEditUiState
import app.coshell.budgets.edit.BudgetKind
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.form.CategoryLine
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.form.categoryLines

/** Which list the limit editor has open. */
enum class BudgetPicker { CATEGORY, CURRENCY }

/** The open list of [picker]: folders or categories by the «Папка | Категория» switch, or the limit currencies. */
@Composable
internal fun BudgetPickers(picker: BudgetPicker?, state: BudgetEditUiState, actions: BudgetEditActions, onClose: () -> Unit) {
    when (picker) {
        null -> Unit
        BudgetPicker.CATEGORY -> ChoiceSheet(
            title = stringResource(if (state.kind == BudgetKind.FOLDER) R.string.budget_choose_folder else R.string.budget_choose_category),
            options = if (state.kind == BudgetKind.FOLDER) {
                state.choices.map { ChoiceOption(it.id, it.name, icon = it.icon, color = it.color, hasBadge = true) }
            } else {
                categoryLines(
                    state.choices.map { CategoryLine(it.id, it.name, it.parent, it.icon, it.color, it.parentId) },
                    stringResource(R.string.transaction_categories_no_folder),
                    stringResource(R.string.category_kind_folder),
                )
            },
            selected = state.categoryId,
            onPick = { actions.onCategorySelect(it); onClose() },
            onDismiss = onClose,
        )
        BudgetPicker.CURRENCY -> ChoiceSheet(
            title = stringResource(R.string.budget_currency),
            options = state.currencies.map { ChoiceOption(it, it) },
            selected = state.currency,
            onPick = { actions.onCurrencySelect(it); onClose() },
            onDismiss = onClose,
        )
    }
}

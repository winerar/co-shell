package app.coshell.budgets.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.budgets.edit.BudgetEditActions
import app.coshell.budgets.edit.BudgetEditUiState
import app.coshell.budgets.edit.BudgetKind
import app.coshell.ui.form.ChoiceDialog
import app.coshell.ui.form.ChoiceOption

/** Which list the limit editor has open. */
enum class BudgetPicker { CATEGORY, CURRENCY }

/** The open list of [picker]: folders or categories by the «Папка | Категория» switch, or the limit currencies. */
@Composable
internal fun BudgetPickers(picker: BudgetPicker?, state: BudgetEditUiState, actions: BudgetEditActions, onClose: () -> Unit) {
    when (picker) {
        null -> Unit
        BudgetPicker.CATEGORY -> ChoiceDialog(
            title = stringResource(if (state.kind == BudgetKind.FOLDER) R.string.budget_choose_folder else R.string.budget_choose_category),
            options = state.choices.map { ChoiceOption(it.id, it.name, it.parent) },
            selected = state.categoryId,
            onPick = { actions.onCategorySelect(it); onClose() },
            onDismiss = onClose,
        )
        BudgetPicker.CURRENCY -> ChoiceDialog(
            title = stringResource(R.string.budget_currency),
            options = state.currencies.map { ChoiceOption(it, it) },
            selected = state.currency,
            onPick = { actions.onCurrencySelect(it); onClose() },
            onDismiss = onClose,
        )
    }
}

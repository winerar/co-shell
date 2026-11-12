package app.coshell.budgets.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.YearMonth

/** Connects [BudgetEditScreen] to [BudgetEditViewModel]; closes when the limit is saved or removed. */
@Composable
fun BudgetEditRoute(
    onDone: () -> Unit,
    /** «Внутри папки»: the editor of a subcategory's own limit, same month. */
    onOpenCategory: (categoryId: String, month: YearMonth) -> Unit,
    viewModel: BudgetEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isDone) { if (state.isDone) onDone() }
    BudgetEditScreen(
        state = state,
        actions = BudgetEditActions(
            onBack = onDone,
            onKindChange = viewModel::onKindChange,
            onCategorySelect = viewModel::onCategorySelect,
            onAmountChange = viewModel::onAmountChange,
            onCurrencySelect = viewModel::onCurrencySelect,
            onRolloverChange = viewModel::onRolloverChange,
            onNoteChange = viewModel::onNoteChange,
            onOpenInside = { id -> state.month?.let { onOpenCategory(id, it) } },
            onSave = viewModel::save,
            onRemove = viewModel::remove,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class BudgetEditActions(
    val onBack: () -> Unit = {},
    val onKindChange: (BudgetKind) -> Unit = {},
    val onCategorySelect: (String) -> Unit = {},
    val onAmountChange: (String) -> Unit = {},
    val onCurrencySelect: (String) -> Unit = {},
    val onRolloverChange: (Boolean) -> Unit = {},
    val onNoteChange: (String) -> Unit = {},
    val onOpenInside: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onRemove: () -> Unit = {},
)

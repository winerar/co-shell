package app.coshell.budgets.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.YearMonth

/** Connects [BudgetsScreen] to [BudgetsViewModel]. */
@Composable
fun BudgetsRoute(
    onBack: () -> Unit,
    /** The limit editor: the category (`null` — a new limit) and the month the change starts from. */
    onEdit: (categoryId: String?, month: YearMonth) -> Unit,
    viewModel: BudgetsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BudgetsScreen(
        state = state,
        actions = BudgetsActions(
            onBack = onBack,
            onEdit = { id -> state.month?.let { onEdit(id, it) } },
            onPreviousMonth = viewModel::onPreviousMonth,
            onNextMonth = viewModel::onNextMonth,
            onFilterChange = viewModel::onFilterChange,
            onToggleFolder = viewModel::onToggleFolder,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class BudgetsActions(
    val onBack: () -> Unit = {},
    /** `null` — «Новый лимит». */
    val onEdit: (String?) -> Unit = {},
    val onPreviousMonth: () -> Unit = {},
    val onNextMonth: () -> Unit = {},
    val onFilterChange: (BudgetFilter) -> Unit = {},
    val onToggleFolder: (String) -> Unit = {},
)

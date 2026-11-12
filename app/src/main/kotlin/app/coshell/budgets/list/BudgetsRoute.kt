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
    /** The envelope screen of a limit in the shown month. */
    onOpen: (categoryId: String, month: YearMonth) -> Unit,
    viewModel: BudgetsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BudgetsScreen(
        state = state,
        actions = BudgetsActions(
            onBack = onBack,
            onEdit = { id -> state.month?.let { onEdit(id, it) } },
            onOpen = { id -> state.month?.let { onOpen(id, it) } },
            onPreviousMonth = viewModel::onPreviousMonth,
            onNextMonth = viewModel::onNextMonth,
            onFilterChange = viewModel::onFilterChange,
            onToggleFolder = viewModel::onToggleFolder,
            onStartDayChange = viewModel::onStartDayChange,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class BudgetsActions(
    val onBack: () -> Unit = {},
    /** `null` — «Новый лимит». */
    val onEdit: (String?) -> Unit = {},
    /** A tap on an envelope. */
    val onOpen: (String) -> Unit = {},
    val onPreviousMonth: () -> Unit = {},
    val onNextMonth: () -> Unit = {},
    val onFilterChange: (BudgetFilter) -> Unit = {},
    val onToggleFolder: (String) -> Unit = {},
    /** «Настройки бюджета» → the day the month starts on. */
    val onStartDayChange: (Int) -> Unit = {},
)

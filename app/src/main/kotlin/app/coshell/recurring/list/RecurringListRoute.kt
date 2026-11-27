package app.coshell.recurring.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [RecurringListScreen] to [RecurringListViewModel]. */
@Composable
fun RecurringListRoute(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onNew: () -> Unit,
    viewModel: RecurringListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RecurringListScreen(
        state = state,
        actions = RecurringListActions(
            onBack = onBack,
            onEdit = onEdit,
            onNew = onNew,
            onQueryChange = viewModel::onQueryChange,
            onFilterChange = viewModel::onFilterChange,
            onModeChange = viewModel::onModeChange,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class RecurringListActions(
    val onBack: () -> Unit = {},
    /** A row: the template editor (the «Повтор» lives there). */
    val onEdit: (String) -> Unit = {},
    val onNew: () -> Unit = {},
    val onQueryChange: (String) -> Unit = {},
    val onFilterChange: (PaymentFilter) -> Unit = {},
    /** «До конца месяца · За год» (#771). */
    val onModeChange: (RecurringMode) -> Unit = {},
)

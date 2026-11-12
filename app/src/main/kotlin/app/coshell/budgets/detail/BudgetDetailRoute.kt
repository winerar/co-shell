package app.coshell.budgets.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.YearMonth

/** Connects [BudgetDetailScreen] to [BudgetDetailViewModel]. */
@Composable
fun BudgetDetailRoute(
    onBack: () -> Unit,
    /** The limit editor for a category (this envelope or one inside the folder), changes from [month] on. */
    onEditLimit: (categoryId: String, month: YearMonth) -> Unit,
    /** Another envelope: a category with its own limit inside this folder. */
    onOpenEnvelope: (categoryId: String, month: YearMonth) -> Unit,
    onTransactionClick: (String) -> Unit,
    viewModel: BudgetDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BudgetDetailScreen(
        state = state,
        actions = BudgetDetailActions(
            onBack = onBack,
            onEditLimit = { id -> state.month?.let { onEditLimit(id, it) } },
            onOpenEnvelope = { id -> state.month?.let { onOpenEnvelope(id, it) } },
            onTransactionClick = onTransactionClick,
        ),
        categoryId = viewModel.categoryId,
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class BudgetDetailActions(
    val onBack: () -> Unit = {},
    val onEditLimit: (String) -> Unit = {},
    val onOpenEnvelope: (String) -> Unit = {},
    val onTransactionClick: (String) -> Unit = {},
)

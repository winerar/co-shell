package app.coshell.transactions.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Key in the «Операции» tab's SavedStateHandle: `true` shows this month's operations without a category
 * (set by «Обзор» → «Внимание»).
 */
const val PRESET_UNCATEGORIZED = "presetUncategorized"

/**
 * Connects [TransactionsScreen] to its ViewModel. [preset] is the tab's SavedStateHandle: another screen
 * may ask there for a ready filter ([PRESET_UNCATEGORIZED]); it is applied once.
 */
@Composable
fun TransactionsRoute(
    onTransactionClick: (String) -> Unit,
    onDuplicate: (String) -> Unit = {},
    preset: SavedStateHandle? = null,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    if (preset != null) {
        val uncategorized by preset.getStateFlow(PRESET_UNCATEGORIZED, false)
            .collectAsStateWithLifecycle()
        LaunchedEffect(uncategorized) {
            if (!uncategorized) return@LaunchedEffect
            viewModel.showUncategorizedThisMonth()
            preset[PRESET_UNCATEGORIZED] = false
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selection by viewModel.selection.collectAsStateWithLifecycle()
    TransactionsScreen(
        state = state,
        selection = selection,
        actions = TransactionsActions(
            onTransactionClick = onTransactionClick,
            onLoadMore = viewModel::loadMore,
            onFilter = viewModel::onFilter,
            onLongPress = viewModel::onLongPress,
            onToggle = viewModel::onToggle,
            onClearSelection = viewModel::clearSelection,
            onDuplicate = { id ->
                viewModel.clearSelection()
                onDuplicate(id)
            },
            onDeleteSelected = viewModel::deleteSelected,
        ),
    )
}

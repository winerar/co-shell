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
            viewModel.onFilter { ListFilter.UncategorizedThisMonth }
            preset[PRESET_UNCATEGORIZED] = false
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    TransactionsScreen(state, onTransactionClick, viewModel::delete, viewModel::loadMore, onDuplicate, viewModel::onFilter)
}

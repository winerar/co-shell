package app.coshell.transactions.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.gate.LocalInputLock
import app.coshell.transactions.list.bulk.BulkEditSheets

/**
 * Key in the «Операции» tab's SavedStateHandle: `true` shows this month's operations without a category
 * (set by «Обзор» → «Внимание»).
 */
const val PRESET_UNCATEGORIZED = "presetUncategorized"

/**
 * Connects [TransactionsScreen] to its ViewModel. [preset] is the tab's SavedStateHandle: another screen
 * may ask there for a ready filter ([PRESET_UNCATEGORIZED], [PRESET_MEMBER]); it is applied once.
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
        val member by preset.getStateFlow<String?>(PRESET_MEMBER, null).collectAsStateWithLifecycle()
        LaunchedEffect(member) {
            val asked = MemberPreset.decode(member) ?: return@LaunchedEffect
            viewModel.show(asked.toFilter())
            preset[PRESET_MEMBER] = null
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    val selection by viewModel.selection.collectAsStateWithLifecycle()
    val peek by viewModel.peek.collectAsStateWithLifecycle()
    val bulk by viewModel.bulk.collectAsStateWithLifecycle()
    TransactionsScreen(
        state = state,
        selection = selection,
        peek = peek,
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
            onEditSelected = lock.guard(viewModel::openBulkEdit),
            onDeleteSelected = lock.guard(viewModel::deleteSelected),
            onPeek = viewModel::onPeek,
            onPeekEnd = viewModel::onPeekEnd,
        ),
    )
    BulkEditSheets(
        state = bulk,
        onStep = viewModel::bulkStep,
        onCategory = lock.guard1(viewModel::applyBulkCategory),
        onTags = lock.guard1(viewModel::applyBulkTags),
        onClose = viewModel::closeBulkEdit,
    )
}

package app.coshell.transactions.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.motion.listItemMotion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.components.DayHeader
import app.coshell.transactions.list.components.FilterBar
import app.coshell.transactions.list.components.SelectionActions
import app.coshell.transactions.list.components.TransactionRow
import app.coshell.ui.bar.ReplaceBottomBar
import app.coshell.ui.list.LoadMoreEffect
import app.coshell.ui.top.SelectionTopBar
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll
import kotlinx.datetime.LocalDate

/**
 * «Операции»: search and filter chips on top, days with their net on the right. Rows have no swipes — a
 * horizontal swipe flips tabs (issue #112); a long press chooses rows (mockup 19В): the header counts them,
 * the bottom bar turns into «Дублировать · Удалить · N», back or «✕» leaves the mode.
 * Older pages load while scrolling ([LoadMoreEffect]); day headers stick to the top. Holding an amount shows
 * it in the report currencies at the operation's day ([peek], #318).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    selection: Selection = Selection(),
    actions: TransactionsActions = TransactionsActions(),
    peek: AmountPeek? = null,
) {
    val noRate = stringResource(R.string.amount_peek_no_rate)
    val scroll = rememberTabScrollBehavior()
    val listState = rememberLazyListState()
    LoadMoreEffect(listState, enabled = state.hasMore, onLoadMore = actions.onLoadMore)
    BackHandler(enabled = selection.active, onBack = actions.onClearSelection)
    ReplaceBottomBar(active = selection.active) {
        SelectionActions(
            count = selection.count,
            onDuplicate = { selection.single?.let(actions.onDuplicate) },
            onDelete = actions.onDeleteSelected,
        )
    }
    Scaffold(
        topBar = {
            if (selection.active) SelectionTopBar(selection.count, actions.onClearSelection, scroll)
            else TabTopBar(stringResource(R.string.nav_transactions), scroll)
        },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        if (state.isLoading) return@Scaffold
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = Sizes.fabClearance),
        ) {
            if (state.days.isNotEmpty() || state.filter.isActive) {
                item(key = "filters") { FilterBar(state, actions.onFilter, Modifier.padding(bottom = Spacing.s)) }
            }
            if (state.days.isEmpty()) {
                item(key = "empty") { EmptyState(filtered = state.filter.isActive, onReset = { actions.onFilter { ListFilter() } }) }
                return@LazyColumn
            }
            state.days.forEach { day ->
                stickyHeader(key = day.date.toString()) { DayHeader(day) }
                items(day.rows, key = { it.id }) { row ->
                    // Rows slide in, out and into place (design-questions §6).
                    Box(listItemMotion()) {
                        TransactionRow(
                            row = row,
                            // While choosing, a tap adds or removes the row instead of opening it.
                            onClick = if (selection.active) actions.onToggle else actions.onTransactionClick,
                            onLongClick = actions.onLongPress,
                            selected = row.id in selection,
                            peek = peek?.takeIf { it.rowId == row.id },
                            onPeek = if (selection.active) null else ({ actions.onPeek(row, day.date, noRate) }),
                            onPeekEnd = actions.onPeekEnd,
                        )
                    }
                }
            }
        }
    }
}

/** Everything the list can ask for; defaults make previews short. */
data class TransactionsActions(
    val onTransactionClick: (String) -> Unit = {},
    val onLoadMore: () -> Unit = {},
    val onFilter: ((ListFilter) -> ListFilter) -> Unit = {},
    val onLongPress: (String) -> Unit = {},
    val onToggle: (String) -> Unit = {},
    val onClearSelection: () -> Unit = {},
    val onDuplicate: (String) -> Unit = {},
    val onDeleteSelected: () -> Unit = {},
    /** Hold the amount of a row of a day (#318); the text is «нет курса». */
    val onPeek: (TransactionsUiState.Row, LocalDate, String) -> Unit = { _, _, _ -> },
    val onPeekEnd: () -> Unit = {},
)

@Composable
private fun EmptyState(filtered: Boolean, onReset: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        Text(
            text = stringResource(if (filtered) R.string.transactions_nothing else R.string.transactions_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (filtered) CoShellTextButton(onClick = onReset) { Text(stringResource(R.string.filter_reset)) }
    }
}

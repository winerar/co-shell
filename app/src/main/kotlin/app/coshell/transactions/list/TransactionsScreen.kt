package app.coshell.transactions.list

import androidx.compose.foundation.ExperimentalFoundationApi
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
import app.coshell.transactions.list.components.SwipeActions
import app.coshell.transactions.list.components.TransactionRow
import app.coshell.ui.list.LoadMoreEffect
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll

/**
 * «Операции»: search and filter chips on top, days with their net on the right; swipe left deletes, right
 * duplicates. Older pages load while scrolling ([LoadMoreEffect]); day headers stick to the top.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    onTransactionClick: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onDuplicate: (String) -> Unit = {},
    onFilter: ((ListFilter) -> ListFilter) -> Unit = {},
) {
    val scroll = rememberTabScrollBehavior()
    val listState = rememberLazyListState()
    LoadMoreEffect(listState, enabled = state.hasMore, onLoadMore = onLoadMore)
    Scaffold(
        topBar = { TabTopBar(stringResource(R.string.nav_transactions), scroll) },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        if (state.isLoading) return@Scaffold
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = Sizes.fabClearance),
        ) {
            if (state.days.isNotEmpty() || state.filter.isActive) {
                item(key = "filters") { FilterBar(state.filter, state.accounts, onFilter, Modifier.padding(bottom = Spacing.s)) }
            }
            if (state.days.isEmpty()) {
                item(key = "empty") { EmptyState(filtered = state.filter.isActive, onReset = { onFilter { ListFilter() } }) }
                return@LazyColumn
            }
            state.days.forEach { day ->
                stickyHeader(key = day.date.toString()) { DayHeader(day) }
                items(day.rows, key = { it.id }) { row ->
                    // Rows slide in, out and into place (design-questions §6).
                    SwipeActions(onDelete = { onDelete(row.id) }, onDuplicate = { onDuplicate(row.id) }, modifier = listItemMotion()) {
                        TransactionRow(row, onTransactionClick)
                    }
                }
            }
        }
    }
}

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

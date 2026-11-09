package app.coshell.transactions.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.transactions.list.components.DayHeader
import app.coshell.transactions.list.components.SwipeToDelete
import app.coshell.transactions.list.components.TransactionRow
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll

/** Day headers stick to the top while their transactions scroll under them. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    onTransactionClick: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
) {
    val scroll = rememberTabScrollBehavior()
    Scaffold(
        topBar = { TabTopBar(stringResource(R.string.nav_transactions), scroll) },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        if (state.isLoading) return@Scaffold
        if (state.days.isEmpty()) {
            EmptyState(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            state.days.forEach { day ->
                stickyHeader(key = day.date.toString()) { DayHeader(day.date, day.relative) }
                items(day.rows, key = { it.id }) { row ->
                    // Rows slide in, out and into place (design-questions §6).
                    SwipeToDelete(onDelete = { onDelete(row.id) }, modifier = Modifier.animateItem()) {
                        TransactionRow(row, onTransactionClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.transactions_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

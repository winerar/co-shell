package app.coshell.accounts.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.accounts.list.components.AccountRow
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll
import app.coshell.core.designsystem.R as DesignR

/** "Accounts" tab: active accounts with balances, archived ones behind a toggle, FAB to add. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    state: AccountsUiState,
    onAccountClick: (String) -> Unit,
    onAddAccount: () -> Unit,
    onToggleArchived: () -> Unit,
) {
    val scroll = rememberTabScrollBehavior()
    Scaffold(
        topBar = { TabTopBar(stringResource(R.string.accounts_title), scroll) },
        modifier = Modifier.tabScroll(scroll),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAccount) {
                Icon(painterResource(DesignR.drawable.ic_add), stringResource(R.string.accounts_add))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.isLoading) return@LazyColumn
            if (state.active.isEmpty() && state.archived.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.accounts_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                return@LazyColumn
            }
            items(state.active, key = { it.id }) { AccountRow(it, onAccountClick, Modifier.animateItem()) }
            if (state.archived.isEmpty()) return@LazyColumn
            item(key = "archived-toggle") {
                TextButton(onClick = onToggleArchived) {
                    Text(
                        stringResource(
                            if (state.showArchived) R.string.accounts_hide_archived else R.string.accounts_show_archived,
                            state.archived.size,
                        ),
                    )
                }
            }
            if (state.showArchived) {
                items(state.archived, key = { it.id }) { AccountRow(it, onAccountClick, Modifier.animateItem()) }
            }
        }
    }
}

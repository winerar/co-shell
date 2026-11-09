package app.coshell.accounts.list

import app.coshell.core.designsystem.motion.listItemMotion
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.tokens.Sizes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.list.components.AccountRow
import app.coshell.core.designsystem.components.CoShellFab
import app.coshell.core.designsystem.components.CoShellTextButton
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
            CoShellFab(onClick = onAddAccount) {
                Icon(painterResource(DesignR.drawable.ic_add), stringResource(R.string.accounts_add))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = Spacing.l, end = Spacing.l, top = Spacing.s, bottom = Sizes.fabClearance),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
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
            items(state.active, key = { it.id }) { AccountRow(it, onAccountClick, listItemMotion()) }
            if (state.archived.isEmpty()) return@LazyColumn
            item(key = "archived-toggle") {
                CoShellTextButton(onClick = onToggleArchived) {
                    Text(
                        stringResource(
                            if (state.showArchived) R.string.accounts_hide_archived else R.string.accounts_show_archived,
                            state.archived.size,
                        ),
                    )
                }
            }
            if (state.showArchived) {
                items(state.archived, key = { it.id }) { AccountRow(it, onAccountClick, listItemMotion()) }
            }
        }
    }
}

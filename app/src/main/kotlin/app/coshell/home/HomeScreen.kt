package app.coshell.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.motion.staggeredEntrance
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.home.components.HomeTiles
import app.coshell.home.components.RecentCard
import app.coshell.home.components.TotalCard
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll

/** Everything «Обзор» can open: the other tabs, the rates screen, an operation, a new account. */
data class HomeActions(
    val onTransactions: () -> Unit = {},
    val onAccounts: () -> Unit = {},
    val onRates: () -> Unit = {},
    /** «Внимание» → «Операции» with the «Без категории · Этот месяц» filter. */
    val onUncategorized: () -> Unit = {},
    val onTransactionClick: (String) -> Unit = {},
    val onAddAccount: () -> Unit = {},
)

@Composable
fun HomeRoute(actions: HomeActions, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state, actions)
}

/**
 * «Обзор» (design-refactor Step 5): hero card with the total, bento tiles 2×2, the latest operations.
 * Blocks come in as a short cascade the first time the tab shows (Step 4).
 */
@Composable
fun HomeScreen(state: HomeUiState, actions: HomeActions = HomeActions()) {
    val scroll = rememberTabScrollBehavior()
    Scaffold(
        topBar = { TabTopBar(stringResource(R.string.nav_home), scroll) },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = Spacing.l, top = Spacing.l, end = Spacing.l, bottom = Sizes.fabClearance),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            if (state.isLoading) return@LazyColumn
            if (state.accounts.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.home_no_accounts),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item { CoShellButton(onClick = actions.onAddAccount) { Text(stringResource(R.string.accounts_add)) } }
                return@LazyColumn
            }
            item { Box(Modifier.staggeredEntrance(0)) { TotalCard(state) } }
            item {
                HomeTiles(
                    state,
                    onTransactions = actions.onTransactions,
                    onAccounts = actions.onAccounts,
                    onRates = actions.onRates,
                    onUncategorized = actions.onUncategorized,
                    modifier = Modifier.staggeredEntrance(1),
                )
            }
            item {
                RecentCard(state.recent, actions.onTransactions, actions.onTransactionClick, Modifier.staggeredEntrance(2))
            }
        }
    }
}

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
import app.coshell.core.designsystem.motion.listItemMotion
import app.coshell.core.designsystem.motion.staggeredEntrance
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.home.budget.HomeBudget
import app.coshell.home.budget.HomeBudgetViewModel
import app.coshell.home.components.HomeTiles
import app.coshell.home.components.HotBudgets
import app.coshell.home.components.RecentCard
import app.coshell.home.components.SyncBanner
import app.coshell.home.components.SyncBannerActions
import app.coshell.home.components.TotalCard
import app.coshell.home.sync.SyncBannerState
import app.coshell.home.sync.SyncBannerViewModel
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll
import kotlinx.datetime.YearMonth

/** Everything «Обзор» can open: «Операции», the rates screen, an operation, a new account. */
data class HomeActions(
    val onTransactions: () -> Unit = {},
    val onRates: () -> Unit = {},
    /** «Без категории» → «Операции» with the «Без категории · Этот месяц» filter. */
    val onUncategorized: () -> Unit = {},
    val onTransactionClick: (String) -> Unit = {},
    val onAddAccount: () -> Unit = {},
    /** Sync banner: «Войти» → «Семья», «Отзыв» → feedback with the error code typed in. */
    val onFamily: () -> Unit = {},
    val onFeedback: (prefill: String) -> Unit = {},
    /** The budget tile → «Бюджеты»; an envelope of «Горит в бюджете» → its screen in the current budget month. */
    val onBudgets: () -> Unit = {},
    val onBudget: (categoryId: String, month: YearMonth) -> Unit = { _, _ -> },
)

@Composable
fun HomeRoute(
    actions: HomeActions,
    viewModel: HomeViewModel = hiltViewModel(),
    syncViewModel: SyncBannerViewModel = hiltViewModel(),
    budgetViewModel: HomeBudgetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val budget by budgetViewModel.state.collectAsStateWithLifecycle()
    val sync by syncViewModel.state.collectAsStateWithLifecycle()
    val syncActions = SyncBannerActions(
        onRetry = syncViewModel::retry,
        onSignIn = actions.onFamily,
        onFeedback = actions.onFeedback,
        onDismiss = syncViewModel::dismiss,
    )
    // Both counts in before the blocks show: the budget tile takes the place of «Без категории».
    HomeScreen(if (budget == null) HomeUiState(isLoading = true) else state, actions, sync, syncActions, budget?.budget)
}

/**
 * «Обзор» (boards `Home`, `HomeAttention`, `BudHome`): hero card with the total, two tiles, «Горит в бюджете»
 * when some limit needs a look, the latest operations. Blocks come in as a short cascade the first time the tab shows (Step 4).
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    actions: HomeActions = HomeActions(),
    sync: SyncBannerState? = null,
    syncActions: SyncBannerActions = SyncBannerActions(),
    budget: HomeBudget? = null,
) {
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
            sync?.let { banner -> item(key = "sync") { SyncBanner(banner, syncActions, listItemMotion()) } }
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
                    onRates = actions.onRates,
                    onUncategorized = actions.onUncategorized,
                    modifier = Modifier.staggeredEntrance(1),
                    budget = budget?.summary,
                    onBudgets = actions.onBudgets,
                )
            }
            budget?.takeIf { it.hot.isNotEmpty() }?.let { hot ->
                item(key = "hot") {
                    HotBudgets(hot.hot, onOpen = { actions.onBudget(it, hot.month) }, modifier = Modifier.staggeredEntrance(2))
                }
            }
            item {
                RecentCard(state.recent, actions.onTransactions, actions.onTransactionClick, Modifier.staggeredEntrance(3))
            }
        }
    }
}

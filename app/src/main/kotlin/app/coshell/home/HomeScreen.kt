package app.coshell.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.accounts.iconRes
import app.coshell.core.designsystem.category.IconBadge
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.home.components.SpendingCard
import app.coshell.home.components.TotalCard
import app.coshell.ui.top.TabTopBar
import app.coshell.ui.top.rememberTabScrollBehavior
import app.coshell.ui.top.tabScroll
import kotlinx.datetime.LocalDate

@Composable
fun HomeRoute(
    onAccountClick: (String) -> Unit,
    onAddAccount: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state, onAccountClick, onAddAccount)
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onAccountClick: (String) -> Unit = {},
    onAddAccount: () -> Unit = {},
) {
    val scroll = rememberTabScrollBehavior()
    Scaffold(
        topBar = { TabTopBar(stringResource(R.string.nav_home), scroll) },
        modifier = Modifier.tabScroll(scroll),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.l),
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
                item {
                    CoShellButton(onClick = onAddAccount) { Text(stringResource(R.string.accounts_add)) }
                }
                return@LazyColumn
            }
            item { TotalCard(state) }
            state.spending?.let { spending -> item { SpendingCard(spending) } }
            item {
                Text(
                    text = stringResource(R.string.home_accounts),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = Spacing.s),
                )
            }
            item { AccountsCard(state.accounts, onAccountClick) }
        }
    }
}

/** All accounts as one list group: name (and "not in the total"), balance on the right; tap opens the editor. */
@Composable
private fun AccountsCard(accounts: List<HomeUiState.AccountRow>, onClick: (String) -> Unit) {
    val palette = LocalAmountPalette.current
    // Rows appear and disappear smoothly: the card's height follows them (design-questions §6). A relayout per
    // frame, but only when an account is added or archived — rare, so it is worth it.
    CoShellCard(Modifier.fillMaxWidth().animateContentSize(Motion.medium())) {
        accounts.forEach { row ->
            key(row.id) {
                CoShellListRow(
                    title = row.name,
                    subtitle = if (row.excludedFromTotal) stringResource(R.string.account_excluded_from_total) else null,
                    leading = { IconBadge(row.type.iconRes, row.color?.let { categoryColor(it) }) },
                    trailing = {
                        AmountText(
                            text = row.balance,
                            style = AmountTextStyle,
                            color = if (row.isNegative) palette.expense else MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = { onClick(row.id) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    CoShellTheme {
        HomeScreen(
            HomeUiState(
                totals = listOf("1 310,50 USD", "1 900 225,00 ARS"),
                rateDate = LocalDate(2026, 11, 9),
                byCurrency = listOf("105 800,50 RUB", "150,00 USD"),
                spending = HomeUiState.MonthSpending(
                    month = LocalDate(2026, 11, 1),
                    totals = listOf("412,30 USD", "598 000,00 ARS"),
                    missing = emptyList(),
                ),
                accounts = listOf(
                    HomeUiState.AccountRow("1", "Карта", "120 800,50 RUB", isNegative = false),
                    HomeUiState.AccountRow("2", "Кредитка", "-15 000,00 RUB", isNegative = true, excludedFromTotal = true),
                ),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    CoShellTheme { HomeScreen(HomeUiState()) }
}

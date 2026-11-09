package app.coshell.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.theme.LocalAmountPalette
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(state.accounts, key = { it.id }) { AccountItem(it, onAccountClick, Modifier.animateItem()) }
        }
    }
}

@Composable
private fun AccountItem(row: HomeUiState.AccountRow, onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalAmountPalette.current
    CoShellCard(modifier = modifier.fillMaxWidth(), onClick = { onClick(row.id) }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(row.name, style = MaterialTheme.typography.bodyLarge)
                if (row.excludedFromTotal) {
                    Text(
                        stringResource(R.string.account_excluded_from_total),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            AmountText(
                text = row.balance,
                style = AmountTextStyle,
                color = if (row.isNegative) palette.expense else MaterialTheme.colorScheme.onSurface,
            )
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
                byCurrency = listOf("105 800,50 ₽", "150,00 USD"),
                spending = HomeUiState.MonthSpending(
                    month = LocalDate(2026, 11, 1),
                    totals = listOf("412,30 USD", "598 000,00 ARS"),
                    missing = emptyList(),
                ),
                accounts = listOf(
                    HomeUiState.AccountRow("1", "Карта", "120 800,50 ₽", isNegative = false),
                    HomeUiState.AccountRow("2", "Кредитка", "-15 000,00 ₽", isNegative = true, excludedFromTotal = true),
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

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.theme.LocalAmountPalette

@Composable
fun HomeRoute(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state)
}

@Composable
fun HomeScreen(state: HomeUiState) {
    Scaffold { padding ->
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
                return@LazyColumn
            }
            item { TotalCard(state.totals) }
            item {
                Text(
                    text = stringResource(R.string.home_accounts),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(state.accounts, key = { it.id }) { AccountItem(it) }
        }
    }
}

@Composable
private fun TotalCard(totals: List<String>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.home_total_balance), style = MaterialTheme.typography.labelLarge)
            totals.forEachIndexed { index, total ->
                Text(
                    text = total,
                    style = if (index == 0) {
                        MaterialTheme.typography.displayMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                )
            }
        }
    }
}

@Composable
private fun AccountItem(row: HomeUiState.AccountRow) {
    val palette = LocalAmountPalette.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(row.name, style = MaterialTheme.typography.bodyLarge)
            Text(
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
                totals = listOf("110 300,50 ₽", "1 250,00 €"),
                accounts = listOf(
                    HomeUiState.AccountRow("1", "Карта", "120 800,50 ₽", isNegative = false),
                    HomeUiState.AccountRow("2", "Кредитка", "-15 000,00 ₽", isNegative = true),
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

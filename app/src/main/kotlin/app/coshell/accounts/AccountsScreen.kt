package app.coshell.accounts

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.model.AccountType
import app.coshell.core.designsystem.R as DesignR

@Composable
fun AccountsRoute(
    onAccountClick: (String) -> Unit,
    onAddAccount: () -> Unit,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountsScreen(state, onAccountClick, onAddAccount, viewModel::toggleArchived)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    state: AccountsUiState,
    onAccountClick: (String) -> Unit,
    onAddAccount: () -> Unit,
    onToggleArchived: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.accounts_title)) }) },
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
            items(state.active, key = { it.id }) { AccountRowItem(it, onAccountClick) }
            if (state.archived.isNotEmpty()) {
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
                    items(state.archived, key = { it.id }) { AccountRowItem(it, onAccountClick) }
                }
            }
        }
    }
}

@Composable
private fun AccountRowItem(row: AccountsUiState.Row, onClick: (String) -> Unit) {
    val palette = LocalAmountPalette.current
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick(row.id) }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(row.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(row.type.labelRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
private fun AccountsScreenPreview() {
    CoShellTheme {
        AccountsScreen(
            state = AccountsUiState(
                active = listOf(
                    AccountsUiState.Row("1", "Наличные", AccountType.CASH, "85 000,00 $", isNegative = false),
                    AccountsUiState.Row("2", "Кредитка", AccountType.CARD, "-15 000,00 ₽", isNegative = true),
                ),
                archived = listOf(AccountsUiState.Row("3", "Старая карта", AccountType.CARD, "0,00 ₽", false)),
                showArchived = true,
            ),
            onAccountClick = {},
            onAddAccount = {},
            onToggleArchived = {},
        )
    }
}

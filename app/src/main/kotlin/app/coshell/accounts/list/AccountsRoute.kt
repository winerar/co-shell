package app.coshell.accounts.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.coshell.accounts.list.components.AccountsOrder
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [AccountsScreen] to its ViewModel. */
@Composable
fun AccountsRoute(
    onBack: (() -> Unit)?,
    onAccountClick: (String) -> Unit,
    onAddAccount: () -> Unit,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var ordering by rememberSaveable { mutableStateOf(false) }
    if (ordering) {
        AccountsOrder(state.active, viewModel::moveAccount) { ordering = false }
        return
    }
    AccountsScreen(
        state,
        AccountsActions(
            onBack = onBack,
            onAccountClick = onAccountClick,
            onAddAccount = onAddAccount,
            onToggleArchived = viewModel::toggleArchived,
            onGrouping = viewModel::onGrouping,
            onToggleGroup = viewModel::onToggleGroup,
            onQuery = viewModel::onQuery,
            onToggleSearch = viewModel::onToggleSearch,
            onOrder = { ordering = true },
        ),
    )
}

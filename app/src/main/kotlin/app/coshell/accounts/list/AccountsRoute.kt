package app.coshell.accounts.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [AccountsScreen] to its ViewModel. */
@Composable
fun AccountsRoute(
    onAccountClick: (String) -> Unit,
    onAddAccount: () -> Unit,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountsScreen(state, onAccountClick, onAddAccount, viewModel::toggleArchived)
}

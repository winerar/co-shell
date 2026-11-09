package app.coshell.transactions.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [TransactionsScreen] to its ViewModel. */
@Composable
fun TransactionsRoute(
    onTransactionClick: (String) -> Unit,
    onDuplicate: (String) -> Unit = {},
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TransactionsScreen(state, onTransactionClick, viewModel::delete, viewModel::loadMore, onDuplicate, viewModel::onFilter)
}

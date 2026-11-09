package app.coshell.accounts.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.model.AccountType

/** Connects [AccountEditScreen] to its ViewModel. */
@Composable
fun AccountEditRoute(onDone: () -> Unit, viewModel: AccountEditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isDone) { if (state.isDone) onDone() }
    AccountEditScreen(
        state = state,
        actions = AccountEditActions(
            onBack = onDone,
            onNameChange = viewModel::onNameChange,
            onCurrencyChange = viewModel::onCurrencyChange,
            onTypeChange = viewModel::onTypeChange,
            onInitialBalanceChange = viewModel::onInitialBalanceChange,
            onIncludedInTotalChange = viewModel::onIncludedInTotalChange,
            onSharedChange = viewModel::onSharedChange,
            onSave = viewModel::save,
            onToggleArchived = viewModel::toggleArchived,
            onDelete = viewModel::delete,
            onDismissDeleteBlocked = viewModel::dismissDeleteBlocked,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class AccountEditActions(
    val onBack: () -> Unit = {},
    val onNameChange: (String) -> Unit = {},
    val onCurrencyChange: (String) -> Unit = {},
    val onTypeChange: (AccountType) -> Unit = {},
    val onInitialBalanceChange: (String) -> Unit = {},
    val onIncludedInTotalChange: (Boolean) -> Unit = {},
    val onSharedChange: (Boolean) -> Unit = {},
    val onSave: () -> Unit = {},
    val onToggleArchived: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onDismissDeleteBlocked: () -> Unit = {},
)

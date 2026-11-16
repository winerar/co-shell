package app.coshell.accounts.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.model.AccountType
import app.coshell.gate.LocalInputLock

/** Connects [AccountEditScreen] to its ViewModel. */
@Composable
fun AccountEditRoute(onDone: () -> Unit, viewModel: AccountEditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    LaunchedEffect(state.isDone) { if (state.isDone) onDone() }
    AccountEditScreen(
        state = state,
        actions = AccountEditActions(
            onNameChange = viewModel::onNameChange,
            onCurrencyChange = viewModel::onCurrencyChange,
            onTypeChange = viewModel::onTypeChange,
            onInitialBalanceChange = viewModel::onInitialBalanceChange,
            onIncludedInTotalChange = viewModel::onIncludedInTotalChange,
            onColorChange = viewModel::onColorChange,
            onSharedChange = viewModel::onSharedChange,
            onSave = lock.guard(viewModel::save),
            onToggleArchived = lock.guard(viewModel::toggleArchived),
            onDelete = lock.guard(viewModel::delete),
            onDismissDeleteBlocked = viewModel::dismissDeleteBlocked,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class AccountEditActions(
    val onNameChange: (String) -> Unit = {},
    val onCurrencyChange: (String) -> Unit = {},
    val onTypeChange: (AccountType) -> Unit = {},
    val onInitialBalanceChange: (String) -> Unit = {},
    val onIncludedInTotalChange: (Boolean) -> Unit = {},
    val onColorChange: (Long?) -> Unit = {},
    val onSharedChange: (Boolean) -> Unit = {},
    val onSave: () -> Unit = {},
    val onToggleArchived: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onDismissDeleteBlocked: () -> Unit = {},
)

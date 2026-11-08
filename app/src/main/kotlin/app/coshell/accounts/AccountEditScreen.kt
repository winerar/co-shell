package app.coshell.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.accounts.AccountEditUiState.BalanceError
import app.coshell.accounts.AccountEditUiState.CurrencyError
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.model.AccountType
import app.coshell.core.designsystem.R as DesignR

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
            onSave = viewModel::save,
            onToggleArchived = viewModel::toggleArchived,
            onDelete = viewModel::delete,
            onDismissDeleteBlocked = viewModel::dismissDeleteBlocked,
        ),
    )
}

data class AccountEditActions(
    val onBack: () -> Unit = {},
    val onNameChange: (String) -> Unit = {},
    val onCurrencyChange: (String) -> Unit = {},
    val onTypeChange: (AccountType) -> Unit = {},
    val onInitialBalanceChange: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onToggleArchived: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onDismissDeleteBlocked: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AccountEditScreen(state: AccountEditUiState, actions: AccountEditActions) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (state.isNew) R.string.account_edit_title_new else R.string.account_edit_title))
                },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
                actions = {
                    TextButton(onClick = actions.onSave, enabled = !state.isLoading) {
                        Text(stringResource(R.string.action_save))
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = actions.onNameChange,
                label = { Text(stringResource(R.string.account_name)) },
                isError = state.nameError,
                supportingText = if (state.nameError) {
                    { Text(stringResource(R.string.account_name_error)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Section(stringResource(R.string.account_currency)) {
                if (!state.currencyLocked) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.suggestedCurrencies.forEach { code ->
                            FilterChip(
                                selected = state.currency == code,
                                onClick = { actions.onCurrencyChange(code) },
                                label = { Text(code) },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = state.currency,
                    onValueChange = actions.onCurrencyChange,
                    label = { Text(stringResource(R.string.account_currency_code)) },
                    enabled = !state.currencyLocked,
                    isError = state.currencyError != null,
                    supportingText = currencySupportingText(state),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Section(stringResource(R.string.account_type)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountType.entries.forEach { type ->
                        FilterChip(
                            selected = state.type == type,
                            onClick = { actions.onTypeChange(type) },
                            label = { Text(stringResource(type.labelRes)) },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.initialBalance,
                onValueChange = actions.onInitialBalanceChange,
                label = { Text(stringResource(R.string.account_initial_balance)) },
                placeholder = { Text("0") },
                isError = state.balanceError != null,
                supportingText = {
                    Text(
                        stringResource(
                            when (state.balanceError) {
                                BalanceError.INVALID -> R.string.account_initial_balance_error
                                BalanceError.TOO_MANY_FRACTION_DIGITS -> R.string.account_initial_balance_fraction_error
                                null -> R.string.account_initial_balance_hint
                            },
                        ),
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )

            Button(onClick = actions.onSave, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_save))
            }

            if (!state.isNew) {
                OutlinedButton(onClick = actions.onToggleArchived, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(if (state.archived) R.string.account_unarchive else R.string.account_archive))
                }
                TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.account_delete), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.account_delete_confirm_title)) },
            text = { Text(stringResource(R.string.account_delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    actions.onDelete()
                }) { Text(stringResource(R.string.account_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    if (state.deleteBlocked) {
        AlertDialog(
            onDismissRequest = actions.onDismissDeleteBlocked,
            title = { Text(stringResource(R.string.account_delete_blocked_title)) },
            text = { Text(stringResource(R.string.account_delete_blocked_text)) },
            confirmButton = {
                TextButton(onClick = {
                    actions.onDismissDeleteBlocked()
                    actions.onToggleArchived()
                }) { Text(stringResource(R.string.account_archive)) }
            },
            dismissButton = {
                TextButton(onClick = actions.onDismissDeleteBlocked) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

private fun currencySupportingText(state: AccountEditUiState): (@Composable () -> Unit)? {
    val res = when {
        state.currencyError == CurrencyError.INVALID -> R.string.account_currency_error
        state.currencyError == CurrencyError.LOCKED || state.currencyLocked -> R.string.account_currency_locked
        else -> return null
    }
    return { Text(stringResource(res)) }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountEditNewPreview() {
    CoShellTheme {
        AccountEditScreen(
            AccountEditUiState(
                currency = "ARS",
                suggestedCurrencies = AccountEditViewModel.DEFAULT_CURRENCIES,
                nameError = true,
            ),
            AccountEditActions(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountEditExistingPreview() {
    CoShellTheme {
        AccountEditScreen(
            AccountEditUiState(
                isNew = false,
                name = "Наличные",
                currency = "USD",
                type = AccountType.CASH,
                initialBalance = "1250,5",
                currencyLocked = true,
            ),
            AccountEditActions(),
        )
    }
}

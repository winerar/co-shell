package app.coshell.transactions.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.transactions.edit.TransactionEditUiState.ToAccountError
import app.coshell.transactions.edit.components.AccountChips
import app.coshell.transactions.edit.components.AmountField
import app.coshell.transactions.edit.components.CategoryChips
import app.coshell.transactions.edit.components.DateChips
import app.coshell.transactions.edit.components.FormSection
import app.coshell.transactions.edit.components.TypeSelector
import app.coshell.core.designsystem.R as DesignR

/**
 * Form order follows how people think about a purchase: type → amount → account → category → date.
 * Each block is a component in `components/`; this file only arranges them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditScreen(state: TransactionEditUiState, actions: TransactionEditActions) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.isNew) R.string.transaction_edit_title_new else R.string.transaction_edit_title,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
                actions = {
                    TextButton(onClick = actions.onSave, enabled = !state.isLoading && state.accounts.isNotEmpty()) {
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (state.accounts.isEmpty()) {
                NoAccounts(actions.onAddAccount)
                return@Column
            }
            TransactionForm(state, actions)
            if (!state.isNew) {
                TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (confirmDelete) {
        DeleteDialog(
            onConfirm = {
                confirmDelete = false
                actions.onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun TransactionForm(state: TransactionEditUiState, actions: TransactionEditActions) {
    val amountFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (state.isNew) amountFocus.requestFocus() }

    TypeSelector(state.type, actions.onTypeChange)

    AmountField(
        value = state.amount,
        onValueChange = actions.onAmountChange,
        label = stringResource(R.string.transaction_amount),
        currencySymbol = state.account?.currencySymbol,
        error = state.amountError,
        modifier = Modifier.fillMaxWidth().focusRequester(amountFocus),
    )

    FormSection(
        title = stringResource(if (state.isTransfer) R.string.transaction_from_account else R.string.transaction_account),
        error = if (state.accountError) stringResource(R.string.transaction_account_required) else null,
    ) {
        AccountChips(state.accounts, state.accountId, actions.onAccountSelect)
    }

    if (state.isTransfer) {
        TransferTarget(state, actions)
    } else {
        FormSection(
            title = stringResource(R.string.transaction_category),
            error = if (state.categoryError) stringResource(R.string.transaction_category_required) else null,
        ) {
            CategoryChips(state.categories, state.categoryId, state.openFolderId, actions.onCategorySelect)
        }
    }

    FormSection(stringResource(R.string.transaction_date)) {
        DateChips(state.date, state.today, state.dateMillisUtc, actions.onDateSelect, actions.onDatePicked)
    }

    OutlinedTextField(
        value = state.note,
        onValueChange = actions.onNoteChange,
        label = { Text(stringResource(R.string.transaction_note)) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier.fillMaxWidth(),
    )

    Button(onClick = actions.onSave, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.action_save))
    }
}

/** Target account of a transfer and, between currencies, the arriving amount. */
@Composable
private fun TransferTarget(state: TransactionEditUiState, actions: TransactionEditActions) {
    FormSection(
        title = stringResource(R.string.transaction_to_account),
        error = when (state.toAccountError) {
            ToAccountError.REQUIRED -> stringResource(R.string.transaction_account_required)
            ToAccountError.SAME_ACCOUNT -> stringResource(R.string.transaction_to_account_same)
            null -> null
        },
    ) {
        AccountChips(state.accounts, state.toAccountId, actions.onToAccountSelect)
    }
    val toAccount = state.toAccount
    if (state.needsToAmount && toAccount != null) {
        AmountField(
            value = state.toAmount,
            onValueChange = actions.onToAmountChange,
            label = stringResource(R.string.transaction_to_amount),
            currencySymbol = toAccount.currencySymbol,
            error = state.toAmountError,
            hint = stringResource(R.string.transaction_to_amount_hint, toAccount.name),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun NoAccounts(onAddAccount: () -> Unit) {
    Text(
        stringResource(R.string.transaction_no_accounts),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Button(onClick = onAddAccount) { Text(stringResource(R.string.accounts_add)) }
}

@Composable
private fun DeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.transaction_delete_confirm_title)) },
        text = { Text(stringResource(R.string.transaction_delete_confirm_text)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_delete)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

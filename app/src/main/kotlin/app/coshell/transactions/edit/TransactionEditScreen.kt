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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.transactions.edit.TransactionEditUiState.ToAccountError
import app.coshell.transactions.edit.components.AccountChips
import app.coshell.transactions.edit.components.AmountField
import app.coshell.transactions.edit.components.CategoryChips
import app.coshell.ui.form.DateChips
import app.coshell.transactions.edit.components.ReceiptSection
import app.coshell.transactions.edit.components.TypeSelector
import app.coshell.ui.form.ConfirmDeleteDialog
import app.coshell.ui.form.DeleteButton
import app.coshell.ui.form.EditTopBar
import app.coshell.ui.form.FormSection

/**
 * Form order follows how people think about a purchase: type → amount → account → category → date.
 * Each block is a component in `components/`; this file only arranges them.
 */
@Composable
fun TransactionEditScreen(state: TransactionEditUiState, actions: TransactionEditActions) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EditTopBar(
                title = stringResource(
                    if (state.isNew) R.string.transaction_edit_title_new else R.string.transaction_edit_title,
                ),
                onBack = actions.onBack,
                onSave = actions.onSave,
                saveEnabled = !state.isLoading && state.accounts.isNotEmpty(),
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
            if (!state.isNew) DeleteButton(onClick = { confirmDelete = true })
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = R.string.transaction_delete_confirm_title,
            text = R.string.transaction_delete_confirm_text,
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
        label = stringResource(if (state.hasReceipt) R.string.transaction_amount_debited else R.string.transaction_amount),
        currencySymbol = state.account?.currencySymbol,
        error = state.amountError,
        hint = state.suggestedRateDate?.takeIf { state.amountSuggested }?.let {
            stringResource(R.string.transaction_amount_suggested, formatDayMonth(it))
        },
        modifier = Modifier.fillMaxWidth().focusRequester(amountFocus),
    )

    if (!state.isTransfer) ReceiptSection(state, actions)

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

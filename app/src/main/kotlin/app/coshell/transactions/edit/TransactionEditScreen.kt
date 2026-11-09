package app.coshell.transactions.edit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.transactions.edit.TransactionEditUiState.ToAccountError
import app.coshell.transactions.edit.components.AccountChips
import app.coshell.transactions.edit.components.AmountField
import app.coshell.transactions.edit.components.CategoryChips
import app.coshell.transactions.edit.components.ReceiptSection
import app.coshell.transactions.edit.components.TypeSelector
import app.coshell.ui.form.DateChips
import app.coshell.ui.form.DeleteButton
import app.coshell.ui.form.FormSection
import app.coshell.ui.format.formatDayMonth
import app.coshell.ui.sheet.EditSheetLayout

/**
 * Form order follows how people think about a purchase: type → amount → account → category → date.
 * Each block is a component in `components/`; this file only arranges them.
 */
@Composable
fun TransactionEditScreen(state: TransactionEditUiState, actions: TransactionEditActions) {
    EditSheetLayout(
        title = stringResource(if (state.isNew) R.string.transaction_edit_title_new else R.string.transaction_edit_title),
        onSave = actions.onSave,
        saveEnabled = !state.isLoading,
        showSave = !state.isLoading && state.accounts.isNotEmpty(),
    ) {
        if (state.isLoading) return@EditSheetLayout
        if (state.accounts.isEmpty()) {
            NoAccounts(actions.onAddAccount)
            return@EditSheetLayout
        }
        TransactionForm(state, actions)
        // Deleted at once; «Отменить» appears in the snackbar after the sheet closes.
        if (!state.isNew) DeleteButton(onClick = actions.onDelete)
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
    CoShellButton(onClick = onAddAccount) { Text(stringResource(R.string.accounts_add)) }
}

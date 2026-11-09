package app.coshell.accounts.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.edit.components.AccountPreviewCard
import app.coshell.accounts.edit.components.AccountTypeSection
import app.coshell.accounts.edit.components.BankColorSection
import app.coshell.accounts.edit.components.CurrencySection
import app.coshell.accounts.edit.components.InitialBalanceField
import app.coshell.ui.form.ConfirmDeleteDialog
import app.coshell.ui.form.DeleteBlockedDialog
import app.coshell.ui.form.EditFooter
import app.coshell.ui.sheet.EditSheetLayout
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.ui.form.ColorSection

/** Account editor layout: name, currency, type, initial balance, "in the total", then save/archive/delete. */
@Composable
fun AccountEditScreen(state: AccountEditUiState, actions: AccountEditActions) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    EditSheetLayout(
        title = stringResource(if (state.isNew) R.string.account_edit_title_new else R.string.account_edit_title),
        onSave = actions.onSave,
        saveEnabled = !state.isLoading,
    ) {
        if (state.isLoading) return@EditSheetLayout
        AccountPreviewCard(state)
        CoShellTextField(
            value = state.name,
            onValueChange = actions.onNameChange,
            label = stringResource(R.string.account_name),
            error = if (state.nameError) stringResource(R.string.account_name_error) else null,
        )
        CurrencySection(state, actions.onCurrencyChange)
        AccountTypeSection(state.type, actions.onTypeChange)
        InitialBalanceField(state.initialBalance, state.balanceError, actions.onInitialBalanceChange)
        BankColorSection(state.name, state.color, actions.onColorChange)
        ColorSection(state.color, actions.onColorChange, allowNone = true)
        CoShellSwitchRow(
            title = stringResource(R.string.account_included_in_total),
            subtitle = stringResource(R.string.account_included_in_total_hint),
            checked = state.includedInTotal,
            onCheckedChange = actions.onIncludedInTotalChange,
        )
        if (state.showSharing) {
            CoShellSwitchRow(
                title = stringResource(R.string.account_shared),
                subtitle = stringResource(R.string.account_shared_hint),
                checked = state.shared,
                onCheckedChange = actions.onSharedChange,
            )
        }
        EditFooter(
            isNew = state.isNew,
            archived = state.archived,
            onToggleArchived = actions.onToggleArchived,
            onDeleteClick = { confirmDelete = true },
        )
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = R.string.account_delete_confirm_title,
            text = R.string.account_delete_confirm_text,
            onConfirm = {
                confirmDelete = false
                actions.onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
    if (state.deleteBlocked) {
        DeleteBlockedDialog(
            title = R.string.account_delete_blocked_title,
            text = R.string.account_delete_blocked_text,
            onArchive = actions.onToggleArchived,
            onDismiss = actions.onDismissDeleteBlocked,
        )
    }
}

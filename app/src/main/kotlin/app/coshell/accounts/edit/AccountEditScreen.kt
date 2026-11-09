package app.coshell.accounts.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.accounts.edit.components.AccountTypeSection
import app.coshell.accounts.edit.components.CurrencySection
import app.coshell.accounts.edit.components.InitialBalanceField
import app.coshell.ui.form.ConfirmDeleteDialog
import app.coshell.ui.form.DeleteBlockedDialog
import app.coshell.ui.form.EditFooter
import app.coshell.ui.form.EditTopBar
import app.coshell.ui.form.NameField
import app.coshell.ui.list.SwitchRow

/** Account editor layout: name, currency, type, initial balance, "in the total", then save/archive/delete. */
@Composable
fun AccountEditScreen(state: AccountEditUiState, actions: AccountEditActions) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EditTopBar(
                title = stringResource(if (state.isNew) R.string.account_edit_title_new else R.string.account_edit_title),
                onBack = actions.onBack,
                onSave = actions.onSave,
                saveEnabled = !state.isLoading,
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
            NameField(
                value = state.name,
                onValueChange = actions.onNameChange,
                label = stringResource(R.string.account_name),
                error = if (state.nameError) stringResource(R.string.account_name_error) else null,
            )
            CurrencySection(state, actions.onCurrencyChange)
            AccountTypeSection(state.type, actions.onTypeChange)
            InitialBalanceField(state.initialBalance, state.balanceError, actions.onInitialBalanceChange)
            SwitchRow(
                icon = null,
                title = stringResource(R.string.account_included_in_total),
                subtitle = stringResource(R.string.account_included_in_total_hint),
                checked = state.includedInTotal,
                onCheckedChange = actions.onIncludedInTotalChange,
            )
            EditFooter(
                isNew = state.isNew,
                archived = state.archived,
                onSave = actions.onSave,
                onToggleArchived = actions.onToggleArchived,
                onDeleteClick = { confirmDelete = true },
            )
        }
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

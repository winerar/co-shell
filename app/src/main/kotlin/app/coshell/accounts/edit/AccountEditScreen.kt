package app.coshell.accounts.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.edit.components.AccountColorSection
import app.coshell.accounts.edit.components.AccountPreviewCard
import app.coshell.accounts.edit.components.AccountTypeSection
import app.coshell.accounts.edit.components.BankPresets
import app.coshell.accounts.edit.components.MoneySection
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.components.CoShellSwitchLine
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.form.ConfirmDeleteDialog
import app.coshell.ui.form.DeleteBlockedDialog
import app.coshell.ui.form.EditTitleActions
import app.coshell.ui.form.IconColorSheet
import app.coshell.ui.sheet.EditSheetLayout

/**
 * Account editor without scrolling (#290, board `Fb290FAAcc`): the live card, the badge beside the name, then one card
 * per entity — «Деньги», «Тип», «Цвет», «Баланс»; archive and delete sit beside the title.
 */
@Composable
fun AccountEditScreen(state: AccountEditUiState, actions: AccountEditActions) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var colorsOpen by rememberSaveable { mutableStateOf(false) }

    EditSheetLayout(
        title = stringResource(if (state.isNew) R.string.account_edit_title_new else R.string.account_edit_title),
        onSave = actions.onSave,
        saveEnabled = !state.isLoading,
        titleAction = { EditTitleActions(state.isNew, state.archived, actions.onToggleArchived) { confirmDelete = true } },
    ) {
        if (state.isLoading) return@EditSheetLayout
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            AccountPreviewCard(state)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                AccountBadge(
                    state.name,
                    state.color,
                    size = Sizes.badgeLarge,
                    modifier = Modifier.tactileClickable(Tactile.Control, CircleShape) { colorsOpen = true },
                )
                CoShellTextField(
                    value = state.name,
                    onValueChange = actions.onNameChange,
                    label = stringResource(R.string.account_name),
                    error = if (state.nameError) stringResource(R.string.account_name_error) else null,
                    modifier = Modifier.weight(1f),
                )
            }
            MoneySection(state, actions)
            AccountTypeSection(state.type, actions.onTypeChange)
            AccountColorSection(state.name, state.color, actions.onColorChange, onOpenAll = { colorsOpen = true })
            CoShellFormSection(stringResource(R.string.account_section_balance)) {
                CoShellSwitchLine(
                    title = stringResource(R.string.account_included_in_total),
                    subtitle = stringResource(R.string.account_included_in_total_short),
                    checked = state.includedInTotal,
                    onCheckedChange = actions.onIncludedInTotalChange,
                )
                if (state.showSharing) {
                    CoShellSwitchLine(
                        title = stringResource(R.string.account_shared),
                        subtitle = stringResource(R.string.account_shared_short),
                        checked = state.shared,
                        onCheckedChange = actions.onSharedChange,
                    )
                }
            }
        }
    }

    if (colorsOpen) {
        IconColorSheet(
            icon = null,
            color = state.color,
            onIconSelect = {},
            onColorSelect = actions.onColorChange,
            onClosed = { colorsOpen = false },
            showIcons = false,
            allowNone = true,
            extra = { BankPresets(state.color, actions.onColorChange) },
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

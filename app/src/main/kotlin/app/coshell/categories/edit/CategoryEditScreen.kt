package app.coshell.categories.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import app.coshell.R
import app.coshell.categories.edit.components.ChildrenSection
import app.coshell.categories.edit.components.FolderSection
import app.coshell.categories.edit.components.LookSections
import app.coshell.categories.edit.components.WhatSection
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.TextKeyboard
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
 * Category editor without scrolling (#290, board `Fb290FA`): the badge beside the name, then one card per entity —
 * «Что это», «Папка» (or «Категории в папке»), «Иконка», «Цвет»; archive and delete sit beside the title.
 */
@Composable
fun CategoryEditScreen(state: CategoryEditUiState, actions: CategoryEditActions) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var lookOpen by rememberSaveable { mutableStateOf(false) }

    EditSheetLayout(
        title = stringResource(state.titleRes),
        onSave = actions.onSave,
        saveEnabled = !state.isLoading,
        titleAction = { EditTitleActions(state.isNew, state.archived, actions.onToggleArchived) { confirmDelete = true } },
    ) {
        if (state.isLoading) return@EditSheetLayout
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            // The badge as it will look in lists, beside the name; a tap opens «Иконка и цвет».
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                CategoryBadge(
                    icon = state.icon,
                    color = state.color,
                    size = Sizes.badgeLarge,
                    modifier = Modifier.tactileClickable(Tactile.Control, CircleShape) { lookOpen = true },
                )
                CoShellTextField(
                    value = state.name,
                    onValueChange = actions.onNameChange,
                    label = stringResource(R.string.category_name),
                    error = if (state.nameError) stringResource(R.string.category_name_error) else null,
                    keyboardOptions = TextKeyboard.copy(imeAction = ImeAction.Done),
                    modifier = Modifier.weight(1f),
                )
            }
            WhatSection(state.asFolder, state.type, state.typeLocked, actions.onKindChange, actions.onTypeChange)
            if (state.asFolder) {
                if (state.candidates.isNotEmpty()) ChildrenSection(state.candidates, state.childIds, actions.onToggleChild)
            } else if (state.canMoveToFolder && state.folders.isNotEmpty()) {
                FolderSection(state.folders, state.parentId, actions.onParentChange)
            }
            LookSections(state.icon, state.color, actions.onIconChange, actions.onColorChange, onOpenAll = { lookOpen = true })
            state.saveError?.let { error ->
                Text(
                    stringResource(error.messageRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    if (lookOpen) {
        IconColorSheet(
            icon = state.icon,
            color = state.color,
            onIconSelect = actions.onIconChange,
            onColorSelect = { picked -> picked?.let(actions.onColorChange) },
            onClosed = { lookOpen = false },
        )
    }
    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = R.string.category_delete_confirm_title,
            text = R.string.category_delete_confirm_text,
            onConfirm = {
                confirmDelete = false
                actions.onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
    if (state.deleteBlocked) {
        DeleteBlockedDialog(
            title = R.string.category_delete_blocked_title,
            text = R.string.category_delete_blocked_text,
            onArchive = actions.onToggleArchived,
            onDismiss = actions.onDismissDeleteBlocked,
        )
    }
}

private val CategoryEditUiState.titleRes: Int
    get() = when {
        isNew && asFolder -> R.string.category_edit_title_new_folder
        isNew -> R.string.category_edit_title_new
        else -> R.string.category_edit_title
    }

package app.coshell.categories.edit

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import app.coshell.R
import app.coshell.categories.edit.components.CategoryTypeSection
import app.coshell.categories.edit.components.ChildrenSection
import app.coshell.categories.edit.components.IconSection
import app.coshell.categories.edit.components.FolderSection
import app.coshell.ui.form.ColorSection
import app.coshell.ui.form.ConfirmDeleteDialog
import app.coshell.ui.form.DeleteBlockedDialog
import app.coshell.ui.form.EditFooter
import app.coshell.ui.sheet.EditSheetLayout
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.TextKeyboard

/** Category editor layout: name, type, icon, colour, folder or subcategories, then save/archive/delete. */
@Composable
fun CategoryEditScreen(state: CategoryEditUiState, actions: CategoryEditActions) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    EditSheetLayout(title = stringResource(state.titleRes), onSave = actions.onSave, saveEnabled = !state.isLoading) {
        if (state.isLoading) return@EditSheetLayout
        CoShellTextField(
            value = state.name,
            onValueChange = actions.onNameChange,
            label = stringResource(R.string.category_name),
            error = if (state.nameError) stringResource(R.string.category_name_error) else null,
            keyboardOptions = TextKeyboard.copy(imeAction = ImeAction.Done),
        )
        CategoryTypeSection(state.type, state.typeLocked, actions.onTypeChange)
        IconSection(state.icon, state.color, actions.onIconChange)
        ColorSection(state.color, { color -> color?.let(actions.onColorChange) })
        if (state.canMoveToFolder && state.folders.isNotEmpty()) {
            FolderSection(state.folders, state.parentId, actions.onParentChange)
        }
        if (state.canHaveChildren && state.candidates.isNotEmpty()) {
            ChildrenSection(state.candidates, state.childIds, actions.onToggleChild)
        }
        state.saveError?.let { error ->
            Text(
                stringResource(error.messageRes),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
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

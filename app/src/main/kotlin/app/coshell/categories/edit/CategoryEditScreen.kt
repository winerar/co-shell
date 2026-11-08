package app.coshell.categories.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.categories.edit.components.CategoryTypeSection
import app.coshell.categories.edit.components.ChildrenSection
import app.coshell.categories.edit.components.ColorSection
import app.coshell.categories.edit.components.IconSection
import app.coshell.categories.edit.components.FolderSection
import app.coshell.ui.form.ConfirmDeleteDialog
import app.coshell.ui.form.DeleteBlockedDialog
import app.coshell.ui.form.EditFooter
import app.coshell.ui.form.EditTopBar
import app.coshell.ui.form.NameField

/** Category editor layout: name, type, icon, colour, folder or subcategories, then save/archive/delete. */
@Composable
fun CategoryEditScreen(state: CategoryEditUiState, actions: CategoryEditActions) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EditTopBar(
                title = stringResource(state.titleRes),
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
                label = stringResource(R.string.category_name),
                error = if (state.nameError) stringResource(R.string.category_name_error) else null,
                imeAction = ImeAction.Done,
            )
            CategoryTypeSection(state.type, state.typeLocked, actions.onTypeChange)
            IconSection(state.icon, state.color, actions.onIconChange)
            ColorSection(state.color, actions.onColorChange)
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
                onSave = actions.onSave,
                onToggleArchived = actions.onToggleArchived,
                onDeleteClick = { confirmDelete = true },
            )
        }
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

package app.coshell.categories

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.SaveCategoryUseCase
import app.coshell.core.model.CategoryType
import app.coshell.core.designsystem.R as DesignR

@Composable
fun CategoryEditRoute(onDone: () -> Unit, viewModel: CategoryEditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isDone) { if (state.isDone) onDone() }
    CategoryEditScreen(
        state = state,
        actions = CategoryEditActions(
            onBack = onDone,
            onNameChange = viewModel::onNameChange,
            onTypeChange = viewModel::onTypeChange,
            onParentChange = viewModel::onParentChange,
            onToggleChild = viewModel::onToggleChild,
            onSave = viewModel::save,
            onToggleArchived = viewModel::toggleArchived,
            onDelete = viewModel::delete,
            onDismissDeleteBlocked = viewModel::dismissDeleteBlocked,
        ),
    )
}

data class CategoryEditActions(
    val onBack: () -> Unit = {},
    val onNameChange: (String) -> Unit = {},
    val onTypeChange: (CategoryType) -> Unit = {},
    val onParentChange: (String?) -> Unit = {},
    val onToggleChild: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onToggleArchived: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onDismissDeleteBlocked: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryEditScreen(state: CategoryEditUiState, actions: CategoryEditActions) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val title = when {
        state.isNew && state.asFolder -> R.string.category_edit_title_new_folder
        state.isNew -> R.string.category_edit_title_new
        else -> R.string.category_edit_title
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(title)) },
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
                label = { Text(stringResource(R.string.category_name)) },
                isError = state.nameError,
                supportingText = if (state.nameError) {
                    { Text(stringResource(R.string.category_name_error)) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Section(stringResource(R.string.category_type)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryType.entries.forEach { type ->
                        FilterChip(
                            selected = state.type == type,
                            onClick = { actions.onTypeChange(type) },
                            enabled = !state.typeLocked,
                            label = { Text(stringResource(type.labelRes)) },
                        )
                    }
                }
                if (state.typeLocked) {
                    Hint(stringResource(R.string.category_type_locked))
                }
            }

            if (state.canMoveToFolder && state.folders.isNotEmpty()) {
                Section(stringResource(R.string.category_folder)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.parentId == null,
                            onClick = { actions.onParentChange(null) },
                            label = { Text(stringResource(R.string.category_folder_none)) },
                        )
                        state.folders.forEach { folder ->
                            FilterChip(
                                selected = state.parentId == folder.id,
                                onClick = { actions.onParentChange(folder.id) },
                                label = { Text(folder.name) },
                            )
                        }
                    }
                }
            }

            if (state.canHaveChildren && state.candidates.isNotEmpty()) {
                Section(stringResource(R.string.category_children)) {
                    Hint(stringResource(R.string.category_children_hint))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.candidates.forEach { candidate ->
                            FilterChip(
                                selected = candidate.id in state.childIds,
                                onClick = { actions.onToggleChild(candidate.id) },
                                label = { Text(candidate.name) },
                            )
                        }
                    }
                }
            }

            state.saveError?.let { error ->
                Text(
                    stringResource(error.messageRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(onClick = actions.onSave, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_save))
            }

            if (!state.isNew) {
                OutlinedButton(onClick = actions.onToggleArchived, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(if (state.archived) R.string.action_unarchive else R.string.action_archive))
                }
                TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.category_delete_confirm_title)) },
            text = { Text(stringResource(R.string.category_delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    actions.onDelete()
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    if (state.deleteBlocked) {
        AlertDialog(
            onDismissRequest = actions.onDismissDeleteBlocked,
            title = { Text(stringResource(R.string.category_delete_blocked_title)) },
            text = { Text(stringResource(R.string.category_delete_blocked_text)) },
            confirmButton = {
                TextButton(onClick = {
                    actions.onDismissDeleteBlocked()
                    actions.onToggleArchived()
                }) { Text(stringResource(R.string.action_archive)) }
            },
            dismissButton = {
                TextButton(onClick = actions.onDismissDeleteBlocked) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

private val SaveCategoryUseCase.Reason.messageRes: Int
    get() = when (this) {
        SaveCategoryUseCase.Reason.PARENT_NOT_FOUND -> R.string.category_error_parent_not_found
        SaveCategoryUseCase.Reason.PARENT_NOT_TOP_LEVEL -> R.string.category_error_parent_nested
        SaveCategoryUseCase.Reason.PARENT_TYPE_MISMATCH -> R.string.category_error_parent_type
        SaveCategoryUseCase.Reason.HAS_SUBCATEGORIES -> R.string.category_error_has_children
        SaveCategoryUseCase.Reason.TYPE_LOCKED -> R.string.category_type_locked
    }

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        content()
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Preview(showBackground = true)
@Composable
private fun CategoryEditFolderPreview() {
    CoShellTheme {
        CategoryEditScreen(
            CategoryEditUiState(
                isNew = true,
                asFolder = true,
                name = "Машина",
                candidates = listOf(
                    CategoryEditUiState.Option("fuel", "Бензин"),
                    CategoryEditUiState.Option("taxi", "Такси"),
                ),
                childIds = setOf("fuel"),
            ),
            CategoryEditActions(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryEditExistingPreview() {
    CoShellTheme {
        CategoryEditScreen(
            CategoryEditUiState(
                isNew = false,
                name = "Бензин",
                typeLocked = true,
                parentId = "car",
                folders = listOf(CategoryEditUiState.Option("car", "Машина")),
            ),
            CategoryEditActions(),
        )
    }
}

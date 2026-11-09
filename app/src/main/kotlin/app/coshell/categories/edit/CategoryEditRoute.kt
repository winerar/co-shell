package app.coshell.categories.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.model.CategoryType

/** Connects [CategoryEditScreen] to its ViewModel. */
@Composable
fun CategoryEditRoute(onDone: () -> Unit, viewModel: CategoryEditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isDone) { if (state.isDone) onDone() }
    CategoryEditScreen(
        state = state,
        actions = CategoryEditActions(
            onNameChange = viewModel::onNameChange,
            onTypeChange = viewModel::onTypeChange,
            onIconChange = viewModel::onIconChange,
            onColorChange = viewModel::onColorChange,
            onParentChange = viewModel::onParentChange,
            onToggleChild = viewModel::onToggleChild,
            onSave = viewModel::save,
            onToggleArchived = viewModel::toggleArchived,
            onDelete = viewModel::delete,
            onDismissDeleteBlocked = viewModel::dismissDeleteBlocked,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class CategoryEditActions(
    val onNameChange: (String) -> Unit = {},
    val onTypeChange: (CategoryType) -> Unit = {},
    val onIconChange: (String) -> Unit = {},
    val onColorChange: (Long) -> Unit = {},
    val onParentChange: (String?) -> Unit = {},
    val onToggleChild: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onToggleArchived: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onDismissDeleteBlocked: () -> Unit = {},
)

package app.coshell.templates.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.model.TransactionType

/** Connects [TemplateEditScreen] to [TemplateEditViewModel]; closes when the template is saved or deleted. */
@Composable
fun TemplateEditRoute(onDone: () -> Unit, viewModel: TemplateEditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isDone) { if (state.isDone) onDone() }
    TemplateEditScreen(
        state = state,
        actions = TemplateEditActions(
            onBack = onDone,
            onTypeChange = viewModel::onTypeChange,
            onNameChange = viewModel::onNameChange,
            onAccountSelect = viewModel::onAccountSelect,
            onToAccountSelect = viewModel::onToAccountSelect,
            onAmountChange = viewModel::onAmountChange,
            onCategorySelect = viewModel::onCategorySelect,
            onNoteChange = viewModel::onNoteChange,
            onSave = viewModel::save,
            onDelete = viewModel::delete,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class TemplateEditActions(
    val onBack: () -> Unit = {},
    val onTypeChange: (TransactionType) -> Unit = {},
    val onNameChange: (String) -> Unit = {},
    val onAccountSelect: (String) -> Unit = {},
    val onToAccountSelect: (String) -> Unit = {},
    val onAmountChange: (String) -> Unit = {},
    val onCategorySelect: (String?) -> Unit = {},
    val onNoteChange: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onDelete: () -> Unit = {},
)

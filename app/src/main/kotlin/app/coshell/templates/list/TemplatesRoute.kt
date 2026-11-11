package app.coshell.templates.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.model.TransactionType

/** Connects [TemplatesScreen] to [TemplatesViewModel]. */
@Composable
fun TemplatesRoute(
    onBack: () -> Unit,
    onUse: (String) -> Unit,
    onEdit: (String) -> Unit,
    onNew: () -> Unit,
    viewModel: TemplatesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TemplatesScreen(
        state = state,
        actions = TemplatesActions(
            onBack = onBack,
            onUse = onUse,
            onEdit = onEdit,
            onNew = onNew,
            onQueryChange = viewModel::onQueryChange,
            onFilterChange = viewModel::onFilterChange,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class TemplatesActions(
    val onBack: () -> Unit = {},
    /** Tap: the «+» sheet filled from the template. */
    val onUse: (String) -> Unit = {},
    /** Long press: the template editor. */
    val onEdit: (String) -> Unit = {},
    val onNew: () -> Unit = {},
    val onQueryChange: (String) -> Unit = {},
    val onFilterChange: (TransactionType?) -> Unit = {},
)

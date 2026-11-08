package app.coshell.categories.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.model.CategoryType
import app.coshell.navigation.CategoryEditDestination

/** Connects [CategoriesScreen] to its ViewModel. */
@Composable
fun CategoriesRoute(
    onBack: () -> Unit,
    onOpen: (CategoryEditDestination) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CategoriesScreen(
        state = state,
        actions = CategoriesActions(
            onBack = onBack,
            onSelectType = viewModel::selectType,
            onToggleFolder = viewModel::toggleFolder,
            onToggleArchived = viewModel::toggleArchived,
            onOpen = onOpen,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class CategoriesActions(
    val onBack: () -> Unit = {},
    val onSelectType: (CategoryType) -> Unit = {},
    val onToggleFolder: (String) -> Unit = {},
    val onToggleArchived: () -> Unit = {},
    /** Opens the editor: existing category, new one, new folder or new one inside a folder. */
    val onOpen: (CategoryEditDestination) -> Unit = {},
)

package app.coshell.categories.list

import app.coshell.core.model.SystemCategories
import app.coshell.categories.list.components.CategoriesOrder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
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
    var ordering by rememberSaveable { mutableStateOf(false) }
    if (ordering) {
        CategoriesOrder(
            rows = orderRows(state.groups, SystemCategories::isAdjustment),
            onMove = viewModel::moveCategory,
            canNest = viewModel::canNest,
            onDone = { ordering = false },
        )
        return
    }
    CategoriesScreen(
        state = state,
        actions = CategoriesActions(
            onBack = onBack,
            onSelectType = viewModel::selectType,
            onToggleFolder = viewModel::toggleFolder,
            onToggleArchived = viewModel::toggleArchived,
            onOpen = onOpen,
            onOrder = { ordering = true },
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
    /** «Изменить порядок» (#338). */
    val onOrder: () -> Unit = {},
)

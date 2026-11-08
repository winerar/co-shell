package app.coshell.categories.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CATEGORY_ORDER
import app.coshell.core.model.buildCategoryTree
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class CategoriesViewModel @Inject constructor(categories: CategoryRepository) : ViewModel() {

    private data class Ui(
        val type: CategoryType = CategoryType.EXPENSE,
        val expanded: Set<String> = emptySet(),
        val showArchived: Boolean = false,
    )

    private val ui = MutableStateFlow(Ui())

    val state: StateFlow<CategoriesUiState> =
        combine(categories.observeCategories(includeArchived = true), ui) { all, ui -> buildState(all, ui) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState(isLoading = true))

    fun selectType(type: CategoryType) = ui.update { it.copy(type = type) }

    fun toggleFolder(id: String) = ui.update {
        it.copy(expanded = if (id in it.expanded) it.expanded - id else it.expanded + id)
    }

    fun toggleArchived() = ui.update { it.copy(showArchived = !it.showArchived) }

    private fun buildState(all: List<Category>, ui: Ui): CategoriesUiState {
        val ofType = all.filter { it.type == ui.type }
        val (archived, active) = ofType.partition { it.archived }
        return CategoriesUiState(
            type = ui.type,
            groups = buildCategoryTree(active).map { node ->
                CategoriesUiState.Group(
                    item = node.category.toItem(),
                    children = node.children.map { it.toItem() },
                    expanded = node.category.id in ui.expanded,
                )
            },
            archived = archived.sortedWith(CATEGORY_ORDER).map { it.toItem() },
            showArchived = ui.showArchived,
        )
    }

    private fun Category.toItem() = CategoriesUiState.Item(id, name, color, icon)
}

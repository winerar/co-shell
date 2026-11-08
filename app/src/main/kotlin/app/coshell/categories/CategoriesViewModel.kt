package app.coshell.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
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
        val children = active.filter { it.parentId != null }.groupBy { it.parentId }
        val activeIds = active.map { it.id }.toSet()
        // A subcategory whose folder is gone (e.g. archived on another phone) is shown at the top level.
        val topLevel = active.filter { it.parentId == null || it.parentId !in activeIds }
        return CategoriesUiState(
            type = ui.type,
            groups = topLevel.sortedWith(ORDER).map { category ->
                CategoriesUiState.Group(
                    item = category.toItem(),
                    children = children[category.id].orEmpty().sortedWith(ORDER).map { it.toItem() },
                    expanded = category.id in ui.expanded,
                )
            },
            archived = archived.sortedWith(ORDER).map { it.toItem() },
            showArchived = ui.showArchived,
        )
    }

    private fun Category.toItem() = CategoriesUiState.Item(id, name, color)

    private companion object {
        val ORDER = compareBy<Category>({ it.sortOrder }, { it.name })
    }
}

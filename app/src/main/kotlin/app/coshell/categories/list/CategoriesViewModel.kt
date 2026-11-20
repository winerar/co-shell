package app.coshell.categories.list

import kotlinx.coroutines.launch
import app.coshell.core.model.MANUAL_CATEGORY_ORDER
import app.coshell.core.domain.order.ReorderUseCase
import app.coshell.core.domain.order.ManualOrder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CATEGORY_ORDER
import app.coshell.core.model.buildCategoryTree
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    categories: CategoryRepository,
    private val reorder: ReorderUseCase? = null,
) : ViewModel() {

    private data class Ui(
        val type: CategoryType = CategoryType.EXPENSE,
        val expanded: Set<String> = emptySet(),
        val showArchived: Boolean = false,
    )

    private val ui = MutableStateFlow(Ui())

    /** Categories just dragged (#338), shown until the database has them — no jump back on drop. */
    private val patch = MutableStateFlow<Map<String, Category>>(emptyMap())

    /** Live categories of the shown type in display order, for «Порядок». */
    private var shownActive: List<Category> = emptyList()

    private val patched = combine(categories.observeCategories(includeArchived = true), patch) { all, local ->
        if (local.isEmpty()) return@combine all
        val saved = all.associateBy { it.id }
        val pending = local.filterValues { c -> saved[c.id]?.let { it.sortOrder != c.sortOrder || it.parentId != c.parentId } ?: false }
        if (pending.size != local.size) patch.value = pending
        if (pending.isEmpty()) all else all.map { pending[it.id] ?: it }.sortedWith(compareBy<Category> { it.type }.then(MANUAL_CATEGORY_ORDER))
    }

    val state: StateFlow<CategoriesUiState> =
        combine(patched, ui) { all, ui -> buildState(all, ui) }
            .offMain()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState(isLoading = true))

    fun selectType(type: CategoryType) = ui.update { it.copy(type = type) }

    fun toggleFolder(id: String) = ui.update {
        it.copy(expanded = if (id in it.expanded) it.expanded - id else it.expanded + id)
    }

    fun toggleArchived() = ui.update { it.copy(showArchived = !it.showArchived) }

    /** «Порядок» (#338): may [movedId] go into [intoId]? */
    fun canNest(movedId: String, intoId: String): Boolean {
        val moved = shownActive.firstOrNull { it.id == movedId } ?: return false
        return ManualOrder.canPlace(shownActive, moved, intoId)
    }

    /** «Порядок» (#338): [movedId] goes to [place]; shown at once, saved for the family. */
    fun moveCategory(movedId: String, place: ManualOrder.Place) {
        val reorder = reorder ?: return
        val shown = shownActive
        viewModelScope.launch {
            val changed = reorder.planCategory(shown, movedId, place)
            if (changed.isEmpty()) return@launch
            patch.update { it + changed.associateBy { c -> c.id } }
            reorder.saveCategories(changed)
        }
    }

    private fun buildState(all: List<Category>, ui: Ui): CategoriesUiState {
        val ofType = all.filter { it.type == ui.type }
        val (archived, active) = ofType.partition { it.archived }
        val tree = buildCategoryTree(active)
        shownActive = tree.flatMap { listOf(it.category) + it.children }
        return CategoriesUiState(
            type = ui.type,
            groups = tree.map { node ->
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

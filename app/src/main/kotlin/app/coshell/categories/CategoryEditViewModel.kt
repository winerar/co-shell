package app.coshell.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.ArchiveCategoryUseCase
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.DeleteCategoryUseCase
import app.coshell.core.domain.SaveCategoryUseCase
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.IdGenerator
import app.coshell.navigation.CategoryEditDestination as Args
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CategoryEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categories: CategoryRepository,
    private val saveCategory: SaveCategoryUseCase,
    private val archiveCategory: ArchiveCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
    private val ids: IdGenerator,
) : ViewModel() {

    private val categoryId: String? = savedStateHandle[Args.ARG_CATEGORY_ID]
    private val newType: CategoryType = savedStateHandle.get<String>(Args.ARG_TYPE)
        ?.let { name -> CategoryType.entries.firstOrNull { it.name == name } } ?: CategoryType.EXPENSE
    private val newParentId: String? = savedStateHandle[Args.ARG_PARENT_ID]
    private val asFolder: Boolean = savedStateHandle[Args.ARG_AS_FOLDER] ?: false

    private var existing: Category? = null
    private var all: List<Category> = emptyList()

    private val _state = MutableStateFlow(CategoryEditUiState(isLoading = true, isNew = categoryId == null))
    val state: StateFlow<CategoryEditUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        all = categories.observeCategories(includeArchived = true).first()
        val category = categoryId?.let { id -> all.firstOrNull { it.id == id } }
        existing = category
        val id = category?.id
        val initial = if (category == null) {
            CategoryEditUiState(isNew = true, asFolder = asFolder, type = newType, parentId = newParentId)
        } else {
            CategoryEditUiState(
                isNew = false,
                name = category.name,
                type = category.type,
                typeLocked = saveCategory.isTypeLocked(category.id),
                parentId = category.parentId,
                childIds = all.filter { it.parentId == id }.map { it.id }.toSet(),
                archived = category.archived,
            )
        }
        _state.value = withOptions(initial)
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, nameError = false, saveError = null) }

    fun onTypeChange(type: CategoryType) = _state.update {
        if (it.typeLocked || it.type == type) it
        else withOptions(it.copy(type = type, parentId = null, childIds = emptySet(), saveError = null))
    }

    fun onParentChange(parentId: String?) = _state.update { withOptions(it.copy(parentId = parentId, saveError = null)) }

    fun onToggleChild(id: String) = _state.update {
        withOptions(it.copy(childIds = if (id in it.childIds) it.childIds - id else it.childIds + id, saveError = null))
    }

    fun save() {
        val form = _state.value
        if (form.isLoading || form.isDone) return
        val name = form.name.trim()
        if (name.isEmpty()) {
            _state.update { it.copy(nameError = true) }
            return
        }
        val base = existing ?: Category(id = ids.newId(), name = name, type = form.type)
        val category = base.copy(name = name, type = form.type, parentId = form.parentId)
        viewModelScope.launch {
            val result = saveCategory(category)
            if (result is SaveCategoryUseCase.Result.Invalid) {
                _state.update { it.copy(saveError = result.reason) }
                return@launch
            }
            if (form.canHaveChildren) moveChildren(category.id, form.childIds)
            _state.update { it.copy(isDone = true) }
        }
    }

    /** Puts the chosen categories into folder [folderId] and takes the unchosen ones out. */
    private suspend fun moveChildren(folderId: String, childIds: Set<String>) {
        categories.observeCategories(includeArchived = true).first().forEach { category ->
            val wanted = category.id in childIds
            val isChild = category.parentId == folderId
            if (wanted != isChild) saveCategory(category.copy(parentId = if (wanted) folderId else null))
        }
    }

    fun toggleArchived() {
        val category = existing ?: return
        viewModelScope.launch {
            archiveCategory(category.id, archived = !category.archived)
            _state.update { it.copy(isDone = true) }
        }
    }

    fun delete() {
        val category = existing ?: return
        viewModelScope.launch {
            when (deleteCategory(category.id)) {
                DeleteCategoryUseCase.Result.DELETED -> _state.update { it.copy(isDone = true) }
                DeleteCategoryUseCase.Result.HAS_TRANSACTIONS -> _state.update { it.copy(deleteBlocked = true) }
            }
        }
    }

    fun dismissDeleteBlocked() = _state.update { it.copy(deleteBlocked = false) }

    /** Recomputes folder and subcategory choices for the current type and selection. */
    private fun withOptions(state: CategoryEditUiState): CategoryEditUiState {
        val selfId = existing?.id
        val topLevel = all.filter { it.type == state.type && !it.archived && it.parentId == null && it.id != selfId }
            .sortedWith(compareBy({ it.sortOrder }, { it.name }))
        val hasChildren = all.filter { it.parentId != null && it.parentId != selfId }.map { it.parentId }.toSet()
        val currentChildren = all.filter { it.parentId == selfId && selfId != null }
        val candidates = topLevel.filter { it.id !in hasChildren } + currentChildren.filter { !it.archived }
        return state.copy(
            isLoading = false,
            folders = if (state.childIds.isEmpty()) topLevel.map { it.toOption() } else emptyList(),
            candidates = if (state.parentId == null) candidates.distinctBy { it.id }.map { it.toOption() } else emptyList(),
        )
    }

    private fun Category.toOption() = CategoryEditUiState.Option(id, name)
}

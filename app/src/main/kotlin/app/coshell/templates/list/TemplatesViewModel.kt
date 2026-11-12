package app.coshell.templates.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateCatalog
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** «Все шаблоны»: the catalog narrowed by the search and the type chips; the list itself — [templateList]. */
@HiltViewModel
class TemplatesViewModel @Inject constructor(catalog: TemplateCatalog) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow<TransactionType?>(null)

    val state: StateFlow<TemplatesUiState> = combine(catalog.observe(), query, filter, ::templateList)
        .offMain()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TemplatesUiState(isLoading = true))

    fun onQueryChange(value: String) { query.value = value }

    fun onFilterChange(type: TransactionType?) { filter.value = type }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

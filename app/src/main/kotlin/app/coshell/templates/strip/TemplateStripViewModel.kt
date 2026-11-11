package app.coshell.templates.strip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.templates.byUsage
import app.coshell.templates.TemplateCatalog
import app.coshell.templates.TemplateItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** [items] — the chips, most used first; [total] — for «Все N ›». */
data class TemplateStripUiState(val items: List<TemplateItem> = emptyList(), val total: Int = 0)

/** The strip of templates over the accounts in the «+» sheet (mockup `TplA`). */
@HiltViewModel
class TemplateStripViewModel @Inject constructor(catalog: TemplateCatalog) : ViewModel() {
    val state: StateFlow<TemplateStripUiState> = catalog.observe()
        .map { snapshot -> stripOf(snapshot) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TemplateStripUiState())

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/** The strip is a shortcut, not the list: the [STRIP_LIMIT] most used, the rest — behind «Все». */
internal fun stripOf(snapshot: TemplateCatalog.Snapshot) = TemplateStripUiState(
    items = snapshot.items.byUsage(snapshot.counts) { it.id }.take(STRIP_LIMIT),
    total = snapshot.items.size,
)

internal const val STRIP_LIMIT = 8

package app.coshell.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.drafts.DraftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** How many drafts wait (#586): the strip on «Обзор» and the count in «Ещё». Apart from the big view models: it only counts. */
@HiltViewModel
class DraftsCountViewModel @Inject constructor(drafts: DraftRepository) : ViewModel() {
    val count: StateFlow<Int> = drafts.observeDrafts().map { it.size }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}

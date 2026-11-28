package app.coshell.newmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Which places wear the «новое» pill now (#335), the ways to them included; a tap on such a place takes its mark away. */
@HiltViewModel
class NewMarksViewModel @Inject constructor(private val center: NewMarkCenter) : ViewModel() {

    val current: StateFlow<Set<String>> = center.marked

    init {
        viewModelScope.launch { center.load() }
    }

    fun opened(key: String) {
        viewModelScope.launch { center.opened(key) }
    }
}

package app.coshell.newmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.newmark.NewMarks
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Which places wear the «новое» pill now (#335); a tap on such a place takes its mark away. */
@HiltViewModel
class NewMarksViewModel @Inject constructor(private val marks: NewMarks) : ViewModel() {

    private val _current = MutableStateFlow<Set<String>>(emptySet())
    val current: StateFlow<Set<String>> = _current.asStateFlow()

    init {
        viewModelScope.launch { _current.value = marks.current(NewFeatures.tracked).intersect(NewFeatures.all.toSet()) }
    }

    fun opened(key: String) {
        if (key !in _current.value) return
        _current.update { it - key }
        viewModelScope.launch { marks.opened(key) }
    }
}

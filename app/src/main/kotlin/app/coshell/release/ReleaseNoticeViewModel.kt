package app.coshell.release

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.newmark.NewMarks
import app.coshell.newmark.NewFeatures
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Whether «Что нового · N историй» stands on «Обзор» (#693, board `Rel693NoticeB`): once after an update, through the «новое»
 * marks (`NewMarks`: nothing on a fresh install, a week at most). «Смотреть» or «✕» put it away for good.
 */
@HiltViewModel
class ReleaseNoticeViewModel @Inject constructor(private val marks: NewMarks) : ViewModel() {

    private val _visible = MutableStateFlow(false)
    val visible: StateFlow<Boolean> = _visible.asStateFlow()

    init {
        viewModelScope.launch {
            _visible.value = ReleaseStories.current.isNotEmpty() && ReleaseStories.KEY in marks.current(NewFeatures.tracked)
        }
    }

    /** Opened or closed: the card goes and does not come back for this set. */
    fun done() {
        if (!_visible.value) return
        _visible.value = false
        viewModelScope.launch { marks.opened(ReleaseStories.KEY) }
    }
}

package app.coshell.notices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.notices.NoticeKind
import app.coshell.core.domain.notices.NoticeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The switches and the hour of the optional notifications in «Уведомления» (#745). Everything is off until the saved value is loaded. */
@HiltViewModel
class NoticeKindsViewModel @Inject constructor(private val settings: NoticeSettings) : ViewModel() {

    val enabled: StateFlow<Set<NoticeKind>> = settings.enabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun setEnabled(kind: NoticeKind, on: Boolean) {
        viewModelScope.launch { settings.setEnabled(kind, on) }
    }

    val time: StateFlow<Int> = settings.time.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoticeSettings.DEFAULT_TIME)

    /** Saves the time; [after] runs once it is written (the daily job is planned again from the saved file). */
    fun setTime(minutes: Int, after: () -> Unit = {}) {
        viewModelScope.launch {
            settings.setTime(minutes)
            after()
        }
    }
}

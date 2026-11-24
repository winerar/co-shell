package app.coshell.access

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** [AccessCenter] for the screens: the code screen, the «Доступ закрыт» window and the input lock. */
@HiltViewModel
class AccessViewModel @Inject constructor(private val center: AccessCenter) : ViewModel() {
    val state = center.state
    val entry = center.entry
    val entering = center.entering
    val note = center.note

    fun start() { viewModelScope.launch { center.start() } }

    fun recheck() { viewModelScope.launch { center.recheck() } }

    fun submit(typed: String) { viewModelScope.launch { center.submit(typed) } }

    fun changeCode() = center.changeCode()

    fun cancelEntering() = center.cancelEntering()

    fun showNote() = center.showNote()

    fun dismissNote() = center.dismissNote()
}

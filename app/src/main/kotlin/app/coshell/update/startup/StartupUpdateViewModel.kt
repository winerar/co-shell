package app.coshell.update.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.update.AppRelease
import app.coshell.core.domain.update.CheckForUpdateUseCase
import app.coshell.core.domain.update.UpdateCheckResult
import app.coshell.core.domain.update.WhatsNewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Dialogs shown over any screen: "what's new" after an update, "update available" (daily check). */
data class StartupUpdateState(val whatsNew: AppRelease? = null, val available: AppRelease? = null)

@HiltViewModel
class StartupUpdateViewModel @Inject constructor(
    private val check: CheckForUpdateUseCase,
    whatsNew: WhatsNewUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StartupUpdateState())
    val state: StateFlow<StartupUpdateState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            whatsNew()?.let { release -> _state.update { it.copy(whatsNew = release) } }
        }
    }

    /** Called every time the app comes to the foreground; the use case limits checks to once a day. */
    fun onAppStart() {
        viewModelScope.launch {
            val result = check()
            if (result is UpdateCheckResult.Available) _state.update { it.copy(available = result.release) }
        }
    }

    fun dismissWhatsNew() = _state.update { it.copy(whatsNew = null) }

    fun dismissAvailable() = _state.update { it.copy(available = null) }
}

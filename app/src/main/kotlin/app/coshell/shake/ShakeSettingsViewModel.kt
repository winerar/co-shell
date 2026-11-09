package app.coshell.shake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.shake.ShakeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The "shake to add a transaction" switch. Read by `CoShellNavHost` (to listen for shakes)
 * and by the More tab (to show and flip it). Off until the saved value is loaded.
 */
@HiltViewModel
class ShakeSettingsViewModel @Inject constructor(private val settings: ShakeSettings) : ViewModel() {

    val enabled: StateFlow<Boolean> =
        settings.enabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch { settings.setEnabled(enabled) }
    }
}

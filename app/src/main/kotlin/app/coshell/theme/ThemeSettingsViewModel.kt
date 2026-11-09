package app.coshell.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.ThemeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The theme setting. Read by `MainActivity` (to pick light/dark) and the More tab (to show and
 * change it). `null` until the saved value is loaded, so the activity does not flash the wrong theme.
 */
@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(private val settings: ThemeSettings) : ViewModel() {

    val mode: StateFlow<ThemeMode?> =
        settings.mode.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setMode(mode) }
    }
}

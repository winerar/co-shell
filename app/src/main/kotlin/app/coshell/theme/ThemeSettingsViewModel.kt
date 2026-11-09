package app.coshell.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.core.domain.settings.ThemeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The theme settings (light/dark mode and colour palette). Read by `MainActivity` (to pick light/dark) and the More tab (to show and
 * change it). `null` until the saved value is loaded, so the activity does not flash the wrong theme.
 */
@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(private val settings: ThemeSettings) : ViewModel() {

    val mode: StateFlow<ThemeMode?> =
        settings.mode.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setMode(mode) }
    }

    /** Colour palette; `null` until loaded, like [mode]. */
    val palette: StateFlow<ThemePalette?> =
        settings.palette.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setPalette(palette: ThemePalette) {
        viewModelScope.launch { settings.setPalette(palette) }
    }
}

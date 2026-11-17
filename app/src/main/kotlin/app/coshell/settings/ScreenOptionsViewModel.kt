package app.coshell.settings

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.settings.ScreenOptions
import app.coshell.core.domain.settings.ScreenOptionsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The options of the screens over the whole app (`MainActivity` provides them); the defaults until the file is read. */
val LocalScreenOptions = staticCompositionLocalOf { ScreenOptions() }

/** «Настройки → Экраны»: shows and changes [ScreenOptions]. */
@HiltViewModel
class ScreenOptionsViewModel @Inject constructor(private val store: ScreenOptionsStore) : ViewModel() {

    val options: StateFlow<ScreenOptions> =
        store.options.stateIn(viewModelScope, SharingStarted.Eagerly, ScreenOptions())

    /** Saves [change]; [after] runs once it is written (for things that read the saved file, like the sync schedule). */
    fun change(after: () -> Unit = {}, change: (ScreenOptions) -> ScreenOptions) {
        viewModelScope.launch {
            store.update(change)
            after()
        }
    }
}

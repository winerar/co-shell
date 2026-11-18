package app.coshell.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.settings.ListDensitySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** «Плотность списков»: read by `MainActivity` (rows of the data lists) and by «Внешний вид» (the switch). Comfortable until loaded. */
@HiltViewModel
class ListDensityViewModel @Inject constructor(private val settings: ListDensitySettings) : ViewModel() {

    val compact: StateFlow<Boolean> =
        settings.compact.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setCompact(compact: Boolean) {
        viewModelScope.launch { settings.setCompact(compact) }
    }
}

package app.coshell.hints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * «Подсказки» and «Показать подсказки заново» in «Ещё → Настройки → Помощь» (#513). Only talks to [HintCenter], so the
 * change reaches «Первые шаги» on «Обзор» at once and nothing else (moments, finale) runs a second time.
 */
@HiltViewModel
class HintsSettingsViewModel @Inject constructor(private val center: HintCenter) : ViewModel() {
    init {
        viewModelScope.launch { center.load() }
    }

    val enabled: StateFlow<Boolean> = center.enabled.map { it ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), center.enabled.value ?: true)

    fun setEnabled(value: Boolean) {
        viewModelScope.launch { center.setEnabled(value) }
    }

    fun reset() {
        viewModelScope.launch { center.reset() }
    }
}

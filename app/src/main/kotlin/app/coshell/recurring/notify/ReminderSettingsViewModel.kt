package app.coshell.recurring.notify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.settings.ReminderSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** «Напоминания о платежах» in «Внешний вид» (#405). On until the saved value is loaded (the default). */
@HiltViewModel
class ReminderSettingsViewModel @Inject constructor(private val settings: ReminderSettings) : ViewModel() {

    val enabled: StateFlow<Boolean> = settings.enabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch { settings.setEnabled(enabled) }
    }

    /** «Время напоминания»; the default until loaded. */
    val hour: StateFlow<Int> = settings.hour.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderSettings.DEFAULT_HOUR)

    /** Saves the hour; [after] runs once it is written (the daily job is planned again from the saved file). */
    fun setHour(hour: Int, after: () -> Unit = {}) {
        viewModelScope.launch {
            settings.setHour(hour)
            after()
        }
    }

    /** «Предупреждать за»: 0 until loaded. */
    val daysBefore: StateFlow<Int> = settings.daysBefore.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setDaysBefore(days: Int) {
        viewModelScope.launch { settings.setDaysBefore(days) }
    }
}

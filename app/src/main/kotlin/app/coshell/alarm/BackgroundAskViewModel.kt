package app.coshell.alarm

import android.content.Context
import androidx.lifecycle.ViewModel
import app.coshell.core.domain.notices.NoticeSettings
import app.coshell.core.domain.settings.ReminderSettings
import app.coshell.core.domain.settings.ScreenOptionsStore
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * «Разрешить работу в фоне?» once on «Обзоре» for people whose reminders were already on before the alarms came (#745):
 * the app was updated (not a fresh install), any payment reminder, optional notice or «Трата партнёра» is on and the question
 * was never answered. Asked also when a
 * reminder is turned on (`BackgroundCheck.askOnEnable`); any answer there counts too. Phone-only, like the notice settings.
 */
@HiltViewModel
class BackgroundAskViewModel @Inject constructor(
    @ApplicationContext context: Context,
    reminders: ReminderSettings,
    notices: NoticeSettings,
    screens: ScreenOptionsStore,
) : ViewModel() {
    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private val asked = MutableStateFlow(prefs.getBoolean(KEY_ASKED, false))
    private val wasUpdated = BackgroundCheck.wasUpdated(context)

    /** Some reminder is on: a payment reminder, an optional notice or «Трата партнёра». */
    val anythingOn: StateFlow<Boolean> = combine(reminders.enabled, notices.enabled, screens.options) { payments, kinds, options ->
        payments || kinds.isNotEmpty() || options.partnerSpend
    }.offMain().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Updated, something reminds and the question was not answered yet; never on a fresh install; `false` until read. */
    val pending: StateFlow<Boolean> = combine(asked, anythingOn) { done, on -> BackgroundCheck.askOnHome(done, wasUpdated, on) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun answered() {
        prefs.edit().putBoolean(KEY_ASKED, true).apply()
        asked.value = true
    }

    private companion object {
        const val FILE = "background_ask"
        const val KEY_ASKED = "asked"
    }
}

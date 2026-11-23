package app.coshell.ai.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.data.auth.AuthGateway
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * «Облачный разбор» (#635): the switch asks first — turning it on opens the question «Включить облачный разбор?» (poll 2026-11-23, «А»),
 * turning it off is at once. Without a sign-in to «Семья» the cloud cannot be asked: the switch is dimmed.
 */
@HiltViewModel
class CloudAiViewModel @Inject constructor(
    private val settings: PrefsCloudAiSettings,
    auth: AuthGateway,
    private val clock: Clock,
    @Named("aiProxyUrl") proxyUrl: String,
) : ViewModel() {
    private val ask = MutableStateFlow(false)
    private val configured = proxyUrl.isNotBlank()

    val state: StateFlow<CloudAiUiState> = combine(settings.enabledFlow, settings.quota, auth.session, ask) { enabled, quota, session, asking ->
        val stage = when {
            session == null -> CloudAiStage.NO_LOGIN
            enabled -> CloudAiStage.ON
            else -> CloudAiStage.OFF
        }
        val today = quota?.takeIf { it.day == today() }
        CloudAiUiState(configured, stage, today?.left, today?.limit, askConsent = asking && stage == CloudAiStage.OFF)
    }.offMain().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CloudAiUiState(configured))

    fun onToggle(on: Boolean) {
        if (on) ask.value = true else settings.setEnabled(false)
    }

    fun onConfirm() {
        ask.value = false
        settings.setEnabled(true)
    }

    fun onDismiss() {
        ask.value = false
    }

    private fun today(): Long = clock.now().toEpochMilliseconds() / DAY_MS

    private companion object {
        const val DAY_MS = 86_400_000L
    }
}

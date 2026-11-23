package app.coshell.privacy.pin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.coshell.R
import app.coshell.core.designsystem.components.keypad.PinKey
import app.coshell.core.domain.hints.HintId
import app.coshell.hints.LocalHintHost
import app.coshell.privacy.PrivacyCenter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** What the PIN screen of the settings does (`PinSetupDestination.mode`). */
enum class PinSetupMode { CREATE, CHANGE, DISABLE }

/**
 * «Вход по PIN» on (CREATE: choose → repeat), «Сменить PIN» (CHANGE: current → choose → repeat) and off (DISABLE: current)
 * — boards `Priv145Set1`, `Priv145Set2`. Closes itself when done; «Назад» cancels.
 */
@HiltViewModel
class PinSetupViewModel @Inject constructor(center: PrivacyCenter, saved: SavedStateHandle) : ViewModel() {
    private val mode = PinSetupMode.entries.firstOrNull { it.name == saved.get<String>("mode") } ?: PinSetupMode.CREATE
    private val flow = PinFlow(
        center,
        viewModelScope,
        start = if (mode == PinSetupMode.CREATE) PinStep.CREATE else PinStep.CURRENT,
        goal = if (mode == PinSetupMode.DISABLE) PinGoal.DISABLE else PinGoal.SET,
    )
    val state = flow.state

    /** A PIN was just turned on (not changed or turned off): the warm moment «Забудете — «Забыли PIN?»» follows (#602, hints.md). */
    val turnsOn: Boolean = mode == PinSetupMode.CREATE

    fun key(key: PinKey) = flow.key(key)
}

@Composable
fun PinSetupRoute(onDone: () -> Unit, viewModel: PinSetupViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hints = LocalHintHost.current
    LaunchedEffect(state.done) {
        if (!state.done) return@LaunchedEffect
        // Offered before leaving: the effect would be cancelled once the screen is gone.
        if (viewModel.turnsOn) hints?.offer(mapOf(HintId.PIN_FORGOT to R.string.hint_pin_forgot))
        onDone()
    }
    Scaffold(Modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.background) { padding ->
        PinLayout(ui = state.toUi(), onKey = viewModel::key, modifier = Modifier.padding(padding))
    }
}

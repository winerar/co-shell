package app.coshell.privacy.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.designsystem.components.keypad.PinKey
import app.coshell.privacy.PrivacyCenter
import app.coshell.privacy.pin.PinFlow
import app.coshell.privacy.pin.PinFlowState
import app.coshell.privacy.pin.PinGoal
import app.coshell.privacy.pin.PinStep
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/** The lock screen (#145): the PIN, and after «Забыли PIN?» + the phone's own lock — a new PIN. */
@HiltViewModel
class LockViewModel @Inject constructor(private val center: PrivacyCenter) : ViewModel() {
    private val flow = PinFlow(center, viewModelScope, PinStep.ENTER, PinGoal.UNLOCK)
    val state: StateFlow<PinFlowState> = flow.state

    fun key(key: PinKey) = flow.key(key)

    fun unlockedByBiometric() {
        center.unlock()
        flow.restart(PinStep.ENTER, PinGoal.UNLOCK)
    }

    /** The phone's own lock confirmed the owner: choose a new PIN; it opens the app when saved. */
    fun deviceConfirmed() = flow.restart(PinStep.CREATE, PinGoal.SET)

    /** The lock screen closed (unlocked): next time it starts at «Введите PIN». */
    fun reset() = flow.restart(PinStep.ENTER, PinGoal.UNLOCK)
}

package app.coshell.privacy.pin

import app.coshell.core.designsystem.components.keypad.PinKey
import app.coshell.core.domain.privacy.PinEntry
import app.coshell.privacy.PinCheck
import app.coshell.privacy.PrivacyCenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A step of a PIN screen: unlock, confirm the current PIN, choose a new one, repeat it. */
enum class PinStep { ENTER, CURRENT, CREATE, REPEAT }

/** Where the steps lead: open the app, save a new PIN, turn the PIN off. */
enum class PinGoal { UNLOCK, SET, DISABLE }

/** The line under the title. */
enum class PinNote { NONE, WRONG, WAIT, MISMATCH }

data class PinFlowState(
    val step: PinStep,
    val entry: PinEntry = PinEntry(),
    val note: PinNote = PinNote.NONE,
    val waitSeconds: Int = 0,
    val errorKey: Int = 0,
    val busy: Boolean = false,
    /** The goal is reached; the screen closes. */
    val done: Boolean = false,
) {
    val error: Boolean get() = note != PinNote.NONE
    val enabled: Boolean get() = !busy && waitSeconds == 0 && !done
}

/**
 * The steps of every PIN screen (#145) as one state machine: the lock screen (ENTER, and «Забыли PIN?» → CREATE → REPEAT)
 * and «Сменить PIN» / «Вход по PIN» in the settings (CURRENT → CREATE → REPEAT, or CURRENT → off). PIN checks and waits —
 * [PrivacyCenter]; a wait after many wrong PINs counts down here second by second with the keys off.
 */
class PinFlow(private val center: PrivacyCenter, private val scope: CoroutineScope, start: PinStep, private var goal: PinGoal) {
    private val mutable = MutableStateFlow(PinFlowState(start))
    val state: StateFlow<PinFlowState> = mutable.asStateFlow()

    private var first = ""
    private var countdown: Job? = null

    fun key(key: PinKey) {
        val s = mutable.value
        if (!s.enabled) return
        when (key) {
            is PinKey.Digit -> {
                val entry = s.entry.type(key.digit)
                // Typing again clears the last error line.
                mutable.value = s.copy(entry = entry, note = if (s.entry.digits.isEmpty()) PinNote.NONE else s.note)
                if (entry.complete) complete(entry.digits)
            }
            PinKey.Backspace -> mutable.value = s.copy(entry = s.entry.erase())
            PinKey.Biometric -> Unit
        }
    }

    /** Starts over at [step] (after «Забыли PIN?» the lock screen sets a new PIN). */
    fun restart(step: PinStep, goal: PinGoal) {
        this.goal = goal
        first = ""
        countdown?.cancel()
        mutable.value = PinFlowState(step)
    }

    private fun complete(pin: String) {
        when (mutable.value.step) {
            PinStep.ENTER, PinStep.CURRENT -> check(pin)
            PinStep.CREATE -> {
                first = pin
                mutable.value = PinFlowState(PinStep.REPEAT)
            }
            PinStep.REPEAT -> if (pin == first) {
                busy { center.setPin(pin); finish() }
            } else {
                first = ""
                mutable.value = PinFlowState(PinStep.CREATE, note = PinNote.MISMATCH, errorKey = mutable.value.errorKey + 1)
            }
        }
    }

    private fun check(pin: String) = busy {
        when (val answer = center.check(pin)) {
            PinCheck.Ok -> when {
                mutable.value.step == PinStep.ENTER -> {
                    center.unlock()
                    finish()
                }
                goal == PinGoal.DISABLE -> {
                    center.removePin()
                    finish()
                }
                else -> mutable.value = PinFlowState(PinStep.CREATE)
            }
            is PinCheck.Wrong -> fail(if (answer.waitMillis > 0) PinNote.WAIT else PinNote.WRONG, answer.waitMillis)
            is PinCheck.Wait -> fail(PinNote.WAIT, answer.waitMillis)
        }
    }

    private fun fail(note: PinNote, waitMillis: Long) {
        mutable.update { it.copy(entry = PinEntry(), note = note, errorKey = it.errorKey + 1, busy = false) }
        if (waitMillis > 0) startCountdown(waitMillis)
    }

    private fun startCountdown(waitMillis: Long) {
        countdown?.cancel()
        countdown = scope.launch {
            var left = ((waitMillis + SECOND - 1) / SECOND).toInt()
            while (left > 0) {
                mutable.update { it.copy(waitSeconds = left) }
                delay(SECOND)
                left--
            }
            mutable.update { it.copy(waitSeconds = 0, note = PinNote.NONE) }
        }
    }

    private fun finish() = mutable.update { it.copy(done = true, busy = false, entry = PinEntry()) }

    private fun busy(block: suspend () -> Unit) {
        mutable.update { it.copy(busy = true) }
        scope.launch { block() }
    }

    private companion object {
        const val SECOND = 1_000L
    }
}

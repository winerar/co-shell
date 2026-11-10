package app.coshell.wipe

import app.coshell.core.domain.DataCounts

/**
 * «Стереть все данные» (board 18, feedback #107).
 * @property counts what will be deleted; `null` while counting.
 * @property word what the user typed; the button unlocks on [CONFIRM_WORD].
 * @property backupName the copy saved before the wipe (shown on the «Данные стёрты» step).
 */
data class WipeDataUiState(
    val counts: DataCounts? = null,
    val word: String = "",
    val step: Step = Step.CONFIRM,
    val backupName: String? = null,
    val backupFailed: Boolean = false,
) {
    enum class Step { CONFIRM, WIPING, DONE }

    val canWipe: Boolean get() = step == Step.CONFIRM && word.trim().equals(CONFIRM_WORD, ignoreCase = true)

    companion object {
        const val CONFIRM_WORD = "СТЕРЕТЬ"
    }
}

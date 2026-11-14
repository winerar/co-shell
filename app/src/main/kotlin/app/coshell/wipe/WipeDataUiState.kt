package app.coshell.wipe

import app.coshell.core.domain.DataCounts

/**
 * «Стереть все данные» (board 18, feedback #107).
 * @property counts what will be deleted; `null` while counting.
 * @property word what the user typed; the button unlocks on any of [CONFIRM_WORDS].
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

    val canWipe: Boolean get() = step == Step.CONFIRM && CONFIRM_WORDS.any { word.trim().equals(it, ignoreCase = true) }

    companion object {
        /**
         * The word to type, in every app language: the screen asks for `R.string.wipe_confirm_word`, and
         * any of these is accepted — so switching the language never locks the button.
         */
        val CONFIRM_WORDS = listOf("СТЕРЕТЬ", "DELETE", "BORRAR")
    }
}

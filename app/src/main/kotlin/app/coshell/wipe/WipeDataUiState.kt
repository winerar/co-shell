package app.coshell.wipe

import app.coshell.core.domain.DataCounts

/**
 * «Стереть все данные» (board 18, feedback #107).
 * @property counts what will be deleted; `null` while counting.
 * @property word what the user typed; the button unlocks on the localized confirm word (`R.string.wipe_confirm_word`).
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

    /** The button unlocks once [word] matches [confirmWord] (the localized word from resources), in any case. */
    fun canWipe(confirmWord: String): Boolean = step == Step.CONFIRM && word.trim().equals(confirmWord, ignoreCase = true)
}

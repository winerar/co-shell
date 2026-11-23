package app.coshell.ai

import androidx.compose.runtime.Immutable

/** Which of the five states of the screen (boards `AiOff`, `AiStep1`, `AiStep2`, `AiOn`, and `AiData` over `AiOff`). */
enum class AiAddonStage { OFF, STEP_ADDON, STEP_MODEL, ON, UNSUPPORTED }

/** «ИИ на телефоне»: the stage, the bar and its already formatted words, what went wrong, whether «Скачать 2,5 ГБ?» is open. */
@Immutable
data class AiAddonUiState(
    val stage: AiAddonStage = AiAddonStage.OFF,
    /** 0..1; `null` — nothing to measure (waiting for Android's «Установить», or the size is not known yet). */
    val progress: Float? = null,
    /** Megabytes downloaded and in total, of the add-on (step 1) or the model (step 2); `0` — not known yet. The screen writes
     *  «1 060 из 2 500 МБ · 42 %» and «осталось 1 440 МБ», like the update screen. */
    val doneMb: Long = 0,
    val totalMb: Long = 0,
    val problem: AiAddonProblem? = null,
    val askData: Boolean = false,
    /** Whole gigabytes of the phone (for «здесь 4»), only meaningful in [AiAddonStage.UNSUPPORTED]. */
    val memoryGb: Int = 0,
) {
    /** Megabytes still to come. */
    val leftMb: Long get() = (totalMb - doneMb).coerceAtLeast(0)
}

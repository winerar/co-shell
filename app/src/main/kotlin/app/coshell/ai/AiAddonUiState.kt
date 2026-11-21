package app.coshell.ai

import androidx.compose.runtime.Immutable

/** Which of the five states of the screen (boards `AiOff`, `AiStep1`, `AiStep2`, `AiOn`, and `AiData` over `AiOff`). */
enum class AiAddonStage { OFF, STEP_ADDON, STEP_MODEL, ON }

/** «ИИ на телефоне»: the stage, the bar and its already formatted words, what went wrong, whether «Скачать 2,5 ГБ?» is open. */
@Immutable
data class AiAddonUiState(
    val stage: AiAddonStage = AiAddonStage.OFF,
    /** 0..1; `null` — nothing to measure (waiting for Android's «Установить», or the size is not known yet). */
    val progress: Float? = null,
    /** Megabytes of the model downloaded and in total (`0` — not known yet); the screen writes «1 060 из 2 500 МБ». */
    val doneMb: Long = 0,
    val totalMb: Long = 0,
    val problem: AiAddonProblem? = null,
    val askData: Boolean = false,
)

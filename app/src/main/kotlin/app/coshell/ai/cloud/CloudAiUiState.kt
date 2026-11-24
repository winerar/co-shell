package app.coshell.ai.cloud

import androidx.compose.runtime.Immutable

/** The three states of «Облачный разбор» (boards `CloudOff`, `CloudOn`, `CloudNoLogin`; `CloudConsent` is [CloudAiUiState.askConsent] over `OFF`). */
enum class CloudAiStage { OFF, ON, NO_LOGIN }

/**
 * [configured] — this build knows the Worker (`AI_PROXY_URL`); without it the row and the screen are hidden. [left] of [limit] — what the cloud
 * still allows today, `null` until the first answer of the day.
 */
@Immutable
data class CloudAiUiState(
    val configured: Boolean = false,
    val stage: CloudAiStage = CloudAiStage.OFF,
    val left: Int? = null,
    val limit: Int? = null,
    val askConsent: Boolean = false,
)

package app.coshell.ai

/**
 * The messages between the co-shell app and this add-on (a bound [android.os.Messenger] service). This file is duplicated in
 * `app/.../ai/AddonProtocol.kt`; `AddonProtocolTest` keeps the two equal — change both together.
 *
 * App → add-on (`what`; `replyTo` = the app's messenger, `arg1` = request id the reply repeats):
 *  - [STATUS]: asks for the state; reply [STATE].
 *  - [DOWNLOAD]: starts downloading the model (replies [PROGRESS], then [STATE]); [CANCEL] stops it; [DELETE] removes the file.
 *  - [COMPLETE]: data `system`, `prompt`; reply [RESULT] with `text` (`error` when the model failed).
 */
object AddonProtocol {
    const val PACKAGE = "app.coshell.ai"
    const val SERVICE = "app.coshell.ai.AddonService"
    const val PERMISSION = "app.coshell.ai.permission.BIND"

    const val STATUS = 1
    const val DOWNLOAD = 2
    const val CANCEL = 3
    const val DELETE = 4
    const val COMPLETE = 5

    const val STATE = 101
    const val PROGRESS = 102
    const val RESULT = 103

    /** `arg2` of [STATE]. */
    const val MODEL_MISSING = 0
    const val MODEL_PARTIAL = 1
    const val MODEL_READY = 2

    const val KEY_SYSTEM = "system"
    const val KEY_PROMPT = "prompt"
    const val KEY_TEXT = "text"
    const val KEY_ERROR = "error"
    const val KEY_DONE = "done"
    const val KEY_TOTAL = "total"
}

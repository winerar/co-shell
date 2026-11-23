package app.coshell.ai.cloud

import javax.inject.Inject
import javax.inject.Singleton

/**
 * A one-shot hand-over (#635): «Быстрые заметки» raises it when the cloud's daily limit ended a request; «Черновики», opened right after,
 * [take]s it and shows the calm line (board `CloudLimit`). Nothing is kept on disk.
 */
@Singleton
class CloudLimitNotice @Inject constructor() {
    @Volatile private var raised = false

    fun raise() { raised = true }

    /** `true` once after [raise]. */
    fun take(): Boolean = raised.also { raised = false }
}

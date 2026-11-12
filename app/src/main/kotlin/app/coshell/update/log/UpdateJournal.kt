package app.coshell.update.log

/**
 * Short log of the last update attempts (#216): what happened between «Обновить» and the new version.
 * Goes into the technical data of a feedback message, so a failed install can be understood without
 * asking. Holds at most [MAX_LINES] lines, oldest dropped first. No amounts or personal data.
 */
interface UpdateJournal {
    fun add(event: String)

    /** Oldest first. */
    fun lines(): List<String>

    /**
     * The system install dialog did not work on this phone once (the user chose «Установить вручную»),
     * so later updates open the Android installer straight away (#217).
     */
    var preferManualInstall: Boolean

    companion object {
        const val MAX_LINES = 40
    }
}

/** Previews, tests and code that does not care. */
object NoUpdateJournal : UpdateJournal {
    override fun add(event: String) = Unit
    override fun lines(): List<String> = emptyList()
    override var preferManualInstall: Boolean = false
}

/** [lines] with [line] added at the end, keeping the last [max] lines. */
internal fun appendCapped(lines: List<String>, line: String, max: Int = UpdateJournal.MAX_LINES): List<String> =
    (lines + line).takeLast(max)

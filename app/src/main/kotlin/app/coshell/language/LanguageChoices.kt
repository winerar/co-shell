package app.coshell.language

/** One line of the language list: [tag] `null` = follow the phone; [keep] — «Оставить русский» after the update. */
data class LanguageChoice(val tag: String?, val keep: Boolean = false)

/** Lines of the language sheet (board Fb352A / Fb352Set / Fb352Update). */
object LanguageChoices {
    /** First launch and «Внешний вид»: «Как в телефоне», then each language. */
    fun all(): List<LanguageChoice> = listOf(LanguageChoice(null)) + AppLanguage.SUPPORTED.map { LanguageChoice(it) }

    /** Once after the update: «Оставить русский» first, then the phone, then the other languages. */
    fun afterUpdate(): List<LanguageChoice> =
        listOf(LanguageChoice(AppLanguage.SUPPORTED.first(), keep = true), LanguageChoice(null)) +
            AppLanguage.SUPPORTED.drop(1).map { LanguageChoice(it) }

    /** Lines for [prompt]; [LanguagePrompt.NONE] is the settings list. */
    fun forPrompt(prompt: LanguagePrompt): List<LanguageChoice> = if (prompt == LanguagePrompt.UPDATE) afterUpdate() else all()
}

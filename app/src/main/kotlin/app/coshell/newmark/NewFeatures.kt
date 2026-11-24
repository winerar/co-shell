package app.coshell.newmark

/**
 * Places that wear the «новое» pill after an update (#335): a key per place, added with the feature in the same change
 * as its `docs/whats-new.md` item. Remove a key a month after its release — the mark has long expired by then.
 * Every key here must sit on a row of «Ещё» (`MoreSections`) — `MoreNewMarksTest` checks it.
 */
object NewFeatures {
    /** «Ещё → Регулярные платежи» (#403). */
    const val RECURRING = "more.recurring"

    /** «Ещё → Долги» (#552). */
    const val DEBTS = "more.debts"

    /** «Ещё → О приложении» (#440). */
    const val ABOUT = "more.about"

    /** «Ещё → Настройки»: «Умные функции → Облачный разбор» inside (#635). */
    const val SETTINGS_CLOUD = "more.settings.cloud"

    val all: List<String> = listOf(RECURRING, DEBTS, ABOUT, SETTINGS_CLOUD)
}

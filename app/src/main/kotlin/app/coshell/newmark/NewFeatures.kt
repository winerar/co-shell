package app.coshell.newmark

import app.coshell.release.ReleaseStories

/**
 * Places that wear the «новое» pill after an update (#335): a key per place, added with the feature in the same change
 * as its `docs/whats-new.md` item. Remove a key a month after its release — the mark has long expired by then.
 * A place on «Ещё» — in [more] (`MoreSections`); a place deeper inside — in [deeper], with its way from «Ещё» in [inside]
 * (owner 2026-11-28: the pill leads to the very row). `MoreNewMarksTest` and `NewFeaturesTest` check both.
 */
object NewFeatures {
    /** «Ещё → Регулярные платежи» (#403). */
    const val RECURRING = "more.recurring"

    /** «Ещё → Долги» (#552). */
    const val DEBTS = "more.debts"

    /** «Ещё → Цели» (#768). */
    const val GOALS = "more.goals"

    /** «Ещё → О приложении» (#440). */
    const val ABOUT = "more.about"

    /** «Ещё → Настройки»: a way to the places inside, marked while one of them is (was a mark of its own until 2026-11-28). */
    const val SETTINGS = "more.settings"

    /** «Настройки → Внешний вид» and «Настройки → Экраны»: ways, as [SETTINGS]. */
    const val SETTINGS_LOOK = "settings.look"
    const val SETTINGS_SCREENS = "settings.screens"

    /** «Внешний вид → Строки в списках» (#775). */
    const val SETTINGS_DENSITY = "settings.density"

    /** «Экраны → Нижняя панель» (#154). */
    const val SETTINGS_BAR = "settings.bar"

    /** The places of «Ещё». */
    val more: List<String> = listOf(RECURRING, DEBTS, GOALS, ABOUT, SETTINGS)

    /** The places deeper inside, each reached through [inside]. */
    val deeper: List<String> = listOf(SETTINGS_DENSITY, SETTINGS_BAR)

    /** Way → the places in it: a way wears the mark while anything inside does (`NewMarkTree`). */
    val inside: Map<String, List<String>> = mapOf(
        SETTINGS to listOf(SETTINGS_LOOK, SETTINGS_SCREENS),
        SETTINGS_LOOK to listOf(SETTINGS_DENSITY),
        SETTINGS_SCREENS to listOf(SETTINGS_BAR),
    )

    /** A deeper place that used to share the mark of [SETTINGS]: it takes over that state, opened or its days (`NewMarks.current`). */
    val inherits: Map<String, String> = mapOf(SETTINGS_DENSITY to SETTINGS, SETTINGS_BAR to SETTINGS)

    /** Every key with a stored state: [SETTINGS] stays for [inherits]. */
    val all: List<String> = more + deeper

    /**
     * Every key `NewMarks` tracks: the places above and the release stories' card (#693). Both view models pass the whole list,
     * so whichever runs first on a fresh install puts all of them away — a fresh install sees nothing as new.
     */
    val tracked: List<String> = all + ReleaseStories.KEY
}

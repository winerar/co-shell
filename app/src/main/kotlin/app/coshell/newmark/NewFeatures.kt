package app.coshell.newmark

import app.coshell.release.ReleaseStories

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

    /** «Ещё → Цели» (#768). */
    const val GOALS = "more.goals"

    /** «Ещё → О приложении» (#440). */
    const val ABOUT = "more.about"

    val all: List<String> = listOf(RECURRING, DEBTS, GOALS, ABOUT)

    /**
     * Every key `NewMarks` tracks: the rows above and the release stories' card (#693). Both view models pass the whole list,
     * so whichever runs first on a fresh install puts all of them away — a fresh install sees nothing as new.
     */
    val tracked: List<String> = all + ReleaseStories.KEY
}

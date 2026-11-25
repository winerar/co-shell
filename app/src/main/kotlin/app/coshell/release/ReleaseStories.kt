package app.coshell.release

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.coshell.R

/**
 * Stories about the main changes of a release (#693; owner poll 2026-11-24: five stories, card on «Обзор»; boards `Rel693*`).
 * Rule: at most [MAX] stories, only the main changes from the top of `docs/whats-new.md`; the rest stays in the list.
 * A new set = new stories here and a new [KEY] (the card then comes back once after the update); `ReleaseStoriesTest`.
 * Each scheme is three vector layers made by `scripts/agent/story_art_693.py`: lines, the theme colour and its hairline.
 */
data class ReleaseStory(
    @StringRes val title: Int,
    @StringRes val text: Int,
    @DrawableRes val ink: Int,
    @DrawableRes val accent: Int,
    @DrawableRes val edge: Int,
)

object ReleaseStories {
    const val MAX = 5

    /** The «новое» key of this set (`NewMarks`): shown after an update, put away on a fresh install, gone once opened or closed. */
    const val KEY = "release.2026-11-25"

    val current: List<ReleaseStory> = listOf(
        ReleaseStory(R.string.release_debts_title, R.string.release_debts_text, R.drawable.story_debts_ink, R.drawable.story_debts_accent, R.drawable.story_debts_edge),
        ReleaseStory(R.string.release_accounts_title, R.string.release_accounts_text, R.drawable.story_accounts_ink, R.drawable.story_accounts_accent, R.drawable.story_accounts_edge),
        ReleaseStory(R.string.release_fan_title, R.string.release_fan_text, R.drawable.story_fan_ink, R.drawable.story_fan_accent, R.drawable.story_fan_edge),
        ReleaseStory(R.string.release_receipt_title, R.string.release_receipt_text, R.drawable.story_receipt_ink, R.drawable.story_receipt_accent, R.drawable.story_receipt_edge),
        ReleaseStory(R.string.release_recap_title, R.string.release_recap_text, R.drawable.story_recap_ink, R.drawable.story_recap_accent, R.drawable.story_recap_edge),
    )
}

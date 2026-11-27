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
    const val KEY = "release.2026-11-27"

    val current: List<ReleaseStory> = listOf(
        ReleaseStory(R.string.release_bulk_title, R.string.release_bulk_text, R.drawable.story_bulk_ink, R.drawable.story_bulk_accent, R.drawable.story_bulk_edge),
        ReleaseStory(R.string.release_repeat_title, R.string.release_repeat_text, R.drawable.story_repeat_ink, R.drawable.story_repeat_accent, R.drawable.story_repeat_edge),
        ReleaseStory(R.string.release_insights_title, R.string.release_insights_text, R.drawable.story_insights_ink, R.drawable.story_insights_accent, R.drawable.story_insights_edge),
        ReleaseStory(R.string.release_bar_title, R.string.release_bar_text, R.drawable.story_bar_ink, R.drawable.story_bar_accent, R.drawable.story_bar_edge),
        ReleaseStory(R.string.release_widgets_title, R.string.release_widgets_text, R.drawable.story_widgets_ink, R.drawable.story_widgets_accent, R.drawable.story_widgets_edge),
    )
}

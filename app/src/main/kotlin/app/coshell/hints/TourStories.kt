package app.coshell.hints

import app.coshell.R
import app.coshell.release.ReleaseStory

/**
 * «Знакомство с приложением» (#598, owner poll 2026-11-25 «А», boards `Tour598S1…S6`): what each screen is for, in the order of
 * creating things — account → entry → categories → limit → reports → family and backup. Each text names the path to the screen;
 * a renamed screen or menu row means a new text here too (`hints.md` golden rule). Schemes — `scripts/agent/story_art_693.py`.
 */
object TourStories {
    val all: List<ReleaseStory> = listOf(
        ReleaseStory(R.string.tour_accounts_title, R.string.tour_accounts_text, R.drawable.story_accounts_ink, R.drawable.story_accounts_accent, R.drawable.story_accounts_edge),
        ReleaseStory(R.string.tour_entry_title, R.string.tour_entry_text, R.drawable.story_fan_ink, R.drawable.story_fan_accent, R.drawable.story_fan_edge),
        ReleaseStory(R.string.tour_categories_title, R.string.tour_categories_text, R.drawable.story_categories_ink, R.drawable.story_categories_accent, R.drawable.story_categories_edge),
        ReleaseStory(R.string.tour_budgets_title, R.string.tour_budgets_text, R.drawable.story_budgets_ink, R.drawable.story_budgets_accent, R.drawable.story_budgets_edge),
        ReleaseStory(R.string.tour_reports_title, R.string.tour_reports_text, R.drawable.story_recap_ink, R.drawable.story_recap_accent, R.drawable.story_recap_edge),
        ReleaseStory(R.string.tour_family_title, R.string.tour_family_text, R.drawable.story_family_ink, R.drawable.story_family_accent, R.drawable.story_family_edge),
    )
}

package app.coshell.feedback

import androidx.annotation.StringRes
import app.coshell.R

/** What a message is about (owner 2026-11-09): the label goes in front of the text so the issue reads at a glance. */
enum class FeedbackKind(@StringRes val label: Int, @StringRes val prompt: Int) {
    BUG(R.string.feedback_kind_bug, R.string.feedback_prompt_bug),
    IDEA(R.string.feedback_kind_idea, R.string.feedback_prompt_idea),
    DESIGN(R.string.feedback_kind_design, R.string.feedback_prompt_design),
    OTHER(R.string.feedback_kind_other, R.string.feedback_prompt_other),
}

/** "Ошибка: шторка дёргается" — the text as sent and as kept in the history. */
fun feedbackMessage(kindLabel: String, text: String): String = "$kindLabel: ${text.trim()}"

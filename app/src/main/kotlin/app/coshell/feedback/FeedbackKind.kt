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

/** «Ошибка: текст» → ("Ошибка", "текст"); a text without a short kind in front → (null, text). */
fun splitFeedbackMessage(message: String): Pair<String?, String> {
    val kind = message.substringBefore(": ", "")
    return if (kind.isNotEmpty() && kind.length <= MAX_KIND_LENGTH && '\n' !in kind) kind to message.substringAfter(": ") else null to message
}

private const val MAX_KIND_LENGTH = 20

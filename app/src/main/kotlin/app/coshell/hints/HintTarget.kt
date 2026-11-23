package app.coshell.hints

import app.coshell.core.domain.hints.HintId

/** Where «Открыть» on a long warm pill leads (`hints.md` §«Тёплые моменты»). */
enum class HintTarget { BACKUP, APPEARANCE, MONTH_RECAP }

/** The screen a moment names in its text, or null when the pill has no action. */
fun HintId.target(): HintTarget? = when (this) {
    HintId.BACKUP -> HintTarget.BACKUP
    HintId.THEME -> HintTarget.APPEARANCE
    HintId.MONTH_END -> HintTarget.MONTH_RECAP
    else -> null
}

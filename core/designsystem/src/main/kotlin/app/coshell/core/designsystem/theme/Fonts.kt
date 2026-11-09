package app.coshell.core.designsystem.theme

import androidx.annotation.FontRes
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import app.coshell.core.designsystem.R

/** One weight of a variable font file (axis `wght`). */
private fun variableFont(@FontRes resId: Int, weight: Int) = Font(
    resId = resId,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/**
 * JetBrains Mono for amounts and exchange rates (owner's choice, issue #25).
 * One variable font file, subset to digits, Latin and currency signs (~77 KB) —
 * see docs/agents/design-system.md §«Шрифты» for how it was built.
 */
val NumbersFontFamily = FontFamily(
    variableFont(R.font.jetbrains_mono, 400),
    variableFont(R.font.jetbrains_mono, 500),
    variableFont(R.font.jetbrains_mono, 600),
)

/**
 * Onest for all words (owner's choice, design-questions §3). Variable, subset to Latin,
 * Cyrillic and punctuation (~77 KB) — design-system.md §«Шрифты».
 */
val TextFontFamily = FontFamily(
    variableFont(R.font.onest, 400),
    variableFont(R.font.onest, 500),
    variableFont(R.font.onest, 600),
)

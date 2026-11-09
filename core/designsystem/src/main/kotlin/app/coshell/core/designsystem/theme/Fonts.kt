package app.coshell.core.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import app.coshell.core.designsystem.R

/**
 * JetBrains Mono for amounts and other numbers (owner's choice, issue #25).
 * One variable font file, subset to digits, Latin and currency signs (~77 KB) —
 * see docs/agents/design-system.md §«Шрифты» for how it was built.
 */
private fun monoFont(weight: Int) = Font(
    resId = R.font.jetbrains_mono,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val NumbersFontFamily = FontFamily(
    monoFont(400),
    monoFont(500),
    monoFont(600),
)

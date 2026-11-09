package app.coshell.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Neon palette (owner's choice, design-questions §2): mint primary + violet accent, no glow.
// Dark theme — bright neon on graphite; light theme — the same hues muted for contrast.

// Mint (primary)
internal val MintNeon = Color(0xFF00E5C0)
internal val MintBright = Color(0xFF5CFFE0)
internal val MintDeep = Color(0xFF00806B)
internal val MintInk = Color(0xFF00382E)
internal val MintShadow = Color(0xFF00473B)
internal val MintPale = Color(0xFFB8FFF0)

// Violet (accent)
internal val VioletNeon = Color(0xFFB388FF)
internal val VioletDeep = Color(0xFF6E3FF0)
internal val VioletInk = Color(0xFF25085E)
internal val VioletShadow = Color(0xFF3A1F7A)
internal val VioletPale = Color(0xFFE6DBFF)

// Sky (tertiary)
internal val SkyNeon = Color(0xFF38D9FF)
internal val SkyDeep = Color(0xFF00709E)
internal val SkyInk = Color(0xFF002E40)
internal val SkyShadow = Color(0xFF004D63)
internal val SkyPale = Color(0xFFC4F1FF)

// Coral (errors; also expenses)
internal val CoralNeon = Color(0xFFFF5C7A)
internal val CoralDeep = Color(0xFFC81E43)
internal val CoralInk = Color(0xFF4A0012)
internal val CoralShadow = Color(0xFF6B0F25)
internal val CoralPale = Color(0xFFFFD9DF)

// Graphite neutrals for the dark theme (background #0B0F14, blocks a step lighter, no borders).
internal val Graphite0 = Color(0xFF080B0F)
internal val Graphite1 = Color(0xFF0B0F14)
internal val Graphite2 = Color(0xFF10151B)
internal val Graphite3 = Color(0xFF151B22)
internal val Graphite4 = Color(0xFF1B222A)
internal val Graphite5 = Color(0xFF222A33)
internal val GraphiteLine = Color(0xFF3A4552)
internal val GraphiteLineSoft = Color(0xFF28313B)
internal val GraphiteText = Color(0xFFE6EDF3)
internal val GraphiteTextMuted = Color(0xFF9AA7B4)

// Cool whites for the light theme.
internal val Frost0 = Color(0xFFFFFFFF)
internal val Frost1 = Color(0xFFF7FAFA)
internal val Frost2 = Color(0xFFF0F4F5)
internal val Frost3 = Color(0xFFEAEFF0)
internal val Frost4 = Color(0xFFE3E9EB)
internal val Frost5 = Color(0xFFDCE4E6)
internal val FrostLine = Color(0xFF75858E)
internal val FrostLineSoft = Color(0xFFC5D0D5)
internal val FrostText = Color(0xFF0F1A1F)
internal val FrostTextMuted = Color(0xFF4A5961)

/** Semantic colors for amounts, independent of the Material scheme. Expense always also gets "−". */
object AmountColors {
    val IncomeLight = Color(0xFF0B7F3D)
    val IncomeDark = Color(0xFF4DFF88)
    val ExpenseLight = CoralDeep
    val ExpenseDark = CoralNeon
}

/** States that Material has no slot for: budget warning (80 %), "no rate", success. */
object StatusColors {
    val WarningLight = Color(0xFF8A5F00)
    val WarningDark = Color(0xFFFFD43B)
    val SuccessLight = AmountColors.IncomeLight
    val SuccessDark = AmountColors.IncomeDark
}

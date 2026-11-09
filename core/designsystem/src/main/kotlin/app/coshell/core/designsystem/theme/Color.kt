package app.coshell.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Neon palette (owner's choice, design-questions §2): mint primary + violet accent, no glow.
// Dark theme — bright neon on graphite. Light theme (issue #62, variant B): brighter, a little acid —
// acid mint containers, acid lime as the second accent, raspberry expenses; mint still leads.

// Mint (primary)
internal val MintNeon = Color(0xFF00E5C0)
internal val MintBright = Color(0xFF5CFFE0)
internal val MintDeep = Color(0xFF00806B)
internal val MintInk = Color(0xFF00382E)
internal val MintShadow = Color(0xFF00473B)
internal val MintPale = Color(0xFFB8FFF0)
/** Light theme containers: more saturated than [MintBright]. */
internal val MintAcid = Color(0xFF1FFFD2)

// Acid lime (light theme second accent: selected chips and segments, the hero gradient's end)
internal val LimeAcid = Color(0xFFD8FF3D)
internal val LimeSoft = Color(0xFF8DFFD9)
internal val LimeDeep = Color(0xFF4A6600)
internal val LimeInk = Color(0xFF1F2A00)

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

// Whites with a faint mint-lime tint for the light theme.
internal val Frost0 = Color(0xFFFFFFFF)
internal val Frost1 = Color(0xFFF6FFF8)
internal val Frost2 = Color(0xFFEDF9F2)
internal val Frost3 = Color(0xFFE5F4EC)
internal val Frost4 = Color(0xFFDDEFE6)
internal val Frost5 = Color(0xFFD5E9DF)
internal val FrostLine = Color(0xFF6C8A7F)
internal val FrostLineSoft = Color(0xFFC2E6D6)
internal val FrostText = Color(0xFF0F1A1F)
internal val FrostTextMuted = Color(0xFF475A54)

/** Semantic colors for amounts, independent of the Material scheme. Expense always also gets "−". */
object AmountColors {
    val IncomeLight = Color(0xFF2E7A00)
    val IncomeDark = Color(0xFF4DFF88)
    /** Raspberry: brighter than [CoralDeep], still ≥ 4.5 on the light background. */
    val ExpenseLight = Color(0xFFD6004A)
    val ExpenseDark = CoralNeon
}

/** States that Material has no slot for: budget warning (80 %), "no rate", success. */
object StatusColors {
    val WarningLight = Color(0xFF8A5F00)
    val WarningDark = Color(0xFFFFD43B)
    val SuccessLight = AmountColors.IncomeLight
    val SuccessDark = AmountColors.IncomeDark
}

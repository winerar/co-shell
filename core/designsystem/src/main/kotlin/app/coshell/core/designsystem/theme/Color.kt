package app.coshell.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// Neon palettes (owner's choice, design-questions §2 and §9): a few colours per palette ([PaletteSpec]),
// the neutrals are derived from its background so each palette keeps its own tint (graphite with a hint
// of green for Volt, eggplant for Mango…). No glow. WCAG contrast — `ColorContrastTest`, every palette.

/** Neutral ladder of one theme: background, blocks a step lighter (dark) or darker (light), lines, text. */
internal data class Neutrals(
    val lowest: Color,
    val background: Color,
    val low: Color,
    val container: Color,
    val high: Color,
    val highest: Color,
    val bright: Color,
    val line: Color,
    val lineSoft: Color,
    val text: Color,
    val textMuted: Color,
)

/** Dark theme: graphite-like blocks lighten towards white in small steps; no borders needed. */
internal fun darkNeutrals(bg: Color) = Neutrals(
    lowest = lerp(bg, Color.Black, 0.35f),
    background = bg,
    low = lerp(bg, Color.White, 0.03f),
    container = lerp(bg, Color.White, 0.055f),
    high = lerp(bg, Color.White, 0.085f),
    highest = lerp(bg, Color.White, 0.115f),
    bright = lerp(bg, Color.White, 0.14f),
    line = lerp(bg, Color.White, 0.26f),
    lineSoft = lerp(bg, Color.White, 0.15f),
    text = lerp(Color.White, bg, 0.08f),
    textMuted = lerp(Color.White, bg, 0.38f),
)

/** Light theme: white with the palette's tint, blocks darken a little, text is near-black of the same tint. */
internal fun lightNeutrals(bg: Color) = Neutrals(
    lowest = Color.White,
    background = bg,
    low = lerp(bg, Color.Black, 0.035f),
    container = lerp(bg, Color.Black, 0.06f),
    high = lerp(bg, Color.Black, 0.085f),
    highest = lerp(bg, Color.Black, 0.11f),
    bright = Color.White,
    line = lerp(bg, Color.Black, 0.48f),
    lineSoft = lerp(bg, Color.Black, 0.17f),
    text = lerp(Color.Black, bg, 0.1f),
    textMuted = lerp(Color.Black, bg, 0.36f),
)

/** States that Material has no slot for: budget warning (80 %), "no rate". Same in every palette. */
object StatusColors {
    val WarningLight = Color(0xFF8A5F00)
    val WarningDark = Color(0xFFFFD43B)
}

// Fixed errors-container tones; the error itself is the palette's expense colour.
internal val CoralShadow = Color(0xFF6B0F25)
internal val CoralPale = Color(0xFFFFD9DF)
internal val CoralInk = Color(0xFF4A0012)

package app.coshell.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// Neon palettes (owner's choice, design-questions §2 and §9): a few colours per palette ([PaletteSpec]),
// the neutrals are derived from its background so each palette keeps its own tint (graphite with a hint
// of green for Lime, eggplant for Mango…). No glow. WCAG contrast — `ColorContrastTest`, every palette.

/** Neutral ladder of one theme: background, blocks a step lighter (dark) or darker (light), lines, text. */
internal data class Neutrals(
    val lowest: Color,
    val background: Color,
    val surface: Color,
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

/**
 * Dark theme (#442): graphite with a hint of the primary; cards ([Neutrals.surface]) a touch of the accent;
 * lines are the accent itself (`outlineVariant` — borders, `outline` — checkbox outlines), saturated enough for 3:1.
 */
internal fun darkNeutrals(bg: Color, primary: Color, accent: Color): Neutrals {
    val page = lerp(bg, primary, 0.06f)
    return Neutrals(
        lowest = lerp(page, Color.Black, 0.35f),
        background = page,
        surface = lerp(page, accent, 0.07f),
        low = lerp(page, Color.White, 0.03f),
        container = lerp(page, accent, 0.14f),
        high = lerp(page, accent, 0.2f),
        highest = lerp(page, accent, 0.26f),
        bright = lerp(page, Color.White, 0.14f),
        line = lerp(page, accent, 0.72f),
        lineSoft = lerp(page, accent, 0.66f),
        text = lerp(Color.White, page, 0.08f),
        textMuted = lerp(Color.White, page, 0.32f),
    )
}

/**
 * Light theme (#442, «Б · Два цвета»): the page is plain white (owner: colour only the components), cards ([Neutrals.surface]) are
 * near-white, blocks, lines and fields take the accent; text is near-black.
 */
internal fun lightNeutrals(bg: Color, accent: Color): Neutrals {
    val page = Color.White
    return Neutrals(
        lowest = Color.White,
        background = page,
        surface = lerp(Color.White, accent, 0.04f),
        low = lerp(page, accent, 0.06f),
        container = lerp(page, accent, 0.1f),
        high = lerp(page, accent, 0.14f),
        highest = lerp(page, accent, 0.18f),
        bright = Color.White,
        line = lerp(page, accent, 0.78f),
        lineSoft = lerp(page, accent, 0.7f),
        text = lerp(Color.Black, bg, 0.1f),
        textMuted = lerp(Color.Black, bg, 0.36f),
    )
}

/** States that Material has no slot for: budget warning (80 %), "no rate". Same in every palette. */
object StatusColors {
    val WarningLight = Color(0xFF7A5400)
    val WarningDark = Color(0xFFFFD43B)
}

// Fixed errors-container tones; the error itself is the palette's expense colour.
internal val CoralShadow = Color(0xFF6B0F25)
internal val CoralPale = Color(0xFFFFD9DF)
internal val CoralInk = Color(0xFF4A0012)

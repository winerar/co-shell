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
    /** The primary as ink: itself in the dark theme, deepened to ≥ 5.4:1 on white in the light one (headings, selected pills, borders). */
    val inkPrimary: Color,
)

/** Deepens [c] towards black until it reaches [min]:1 over [over] (the palette's own shade of a pale neon). */
internal fun deepen(c: Color, over: Color, min: Float): Color {
    var t = 0f
    var r = c
    while (contrastRatio(r, over) < min && t < 1f) {
        t += 0.02f
        r = lerp(c, Color.Black, t)
    }
    return r
}

/** The faintest mix of [from] towards [to] that still reaches [min]:1 over [vs]. */
internal fun mixToContrast(from: Color, to: Color, vs: Color, min: Float): Color {
    var t = 0f
    var r = from
    while (contrastRatio(r, vs) < min && t < 1f) {
        t += 0.02f
        r = lerp(from, to, t)
    }
    return r
}

/**
 * Dark theme (#442): graphite with a hint of the primary; cards ([Neutrals.surface]) a touch of the primary;
 * lines are the primary itself (`outlineVariant` — borders ≥ 3:1, `outline` — checkbox outlines), blocks and fields
 * take the accent. Primary : accent ≈ 2 : 1 across the screen.
 */
internal fun darkNeutrals(bg: Color, primary: Color, accent: Color): Neutrals {
    val page = lerp(bg, primary, 0.06f)
    val surface = lerp(page, primary, 0.07f)
    return Neutrals(
        lowest = lerp(page, Color.Black, 0.35f),
        background = page,
        surface = surface,
        low = lerp(page, Color.White, 0.03f),
        container = lerp(page, accent, 0.14f),
        high = lerp(page, accent, 0.2f),
        highest = lerp(page, accent, 0.26f),
        bright = lerp(page, Color.White, 0.14f),
        line = mixToContrast(page, primary, surface, 4.2f),
        lineSoft = mixToContrast(page, primary, surface, 3.1f),
        text = lerp(Color.White, page, 0.08f),
        textMuted = lerp(Color.White, page, 0.32f),
        inkPrimary = primary,
    )
}

/**
 * Light theme (#442): the page is plain white (owner: colour only the components), cards ([Neutrals.surface]) a wash of the
 * primary itself (no deep shade of it — the owner refused an extra dark accent); lines are near-black of the text tint,
 * blocks and fields take the accent. A fill that melts into its background gets a thin edge (`fillEdge`).
 */
internal fun lightNeutrals(bg: Color, primary: Color, accent: Color): Neutrals {
    val page = Color.White
    val text = lerp(Color.Black, bg, 0.1f)
    val surface = lerp(page, primary, 0.12f)
    return Neutrals(
        lowest = Color.White,
        background = page,
        surface = surface,
        low = lerp(page, accent, 0.06f),
        container = lerp(page, accent, 0.1f),
        high = lerp(page, accent, 0.14f),
        highest = lerp(page, accent, 0.18f),
        bright = Color.White,
        line = mixToContrast(page, text, surface, 4.2f),
        lineSoft = mixToContrast(page, text, surface, 3.1f),
        text = text,
        textMuted = lerp(Color.Black, bg, 0.36f),
        inkPrimary = text,
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

package app.coshell.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import app.coshell.core.designsystem.motion.LocalTactileLight
import app.coshell.core.designsystem.tokens.CoShellShapes

/** Amount colors: income, expense (always with "−" too), transfer (neutral). */
@Immutable
data class AmountPalette(val income: Color, val expense: Color, val transfer: Color)

/** Colors for states Material has no slot for. */
@Immutable
data class StatusPalette(val warning: Color, val success: Color)

val LocalAmountPalette = staticCompositionLocalOf { themeColorsFor(CoShellPalette.Lime, dark = false).amounts }

val LocalStatusPalette = staticCompositionLocalOf { themeColorsFor(CoShellPalette.Lime, dark = false).status }

/**
 * The main block of a screen («Обзор» total, budgets month, envelope head — `CoShellCard(tone = CardTone.Hero)`), filled
 * with a gradient of the palette in both themes. Light: the pastel neon gradient (issue #62) with dark ink; dark:
 * a deep gradient — neon, income and accent sunk into graphite (issue #249, variant Б) — with light text and the
 * main amount in neon. [hint] — grey lines, [track] — the empty part of a bar, [expense] and [warning] — overspent and
 * "careful" over the gradient (the theme's own, pushed further from it so small text keeps 4.5:1).
 */
@Immutable
data class HeroPalette(
    val brush: Brush,
    val content: Color,
    val amount: Color,
    val hint: Color,
    val track: Color,
    val expense: Color,
    val warning: Color,
)

val LocalHeroPalette = staticCompositionLocalOf { themeColorsFor(CoShellPalette.Lime, dark = false).hero }

/** Whether [CoShellTheme] is dark — e.g. to pick a category color's variant. */
val LocalDarkTheme = staticCompositionLocalOf { false }

/**
 * App theme: one of the own neon [CoShellPalette]s (default — acid [CoShellPalette.Lime]), light and dark.
 * Wallpaper colors (Material You) are not used — the app looks the same on both family phones. [darkTheme] comes from the theme setting
 * (system / light / dark).
 */
@Composable
fun CoShellTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: CoShellPalette = CoShellPalette.Lime,
    content: @Composable () -> Unit,
) {
    val colors = remember(palette, darkTheme) { themeColorsFor(palette, darkTheme) }
    val colorScheme = colors.scheme
    val amounts = colors.amounts
    val status = colors.status
    val hero = colors.hero

    // Pressed elements catch light: white on graphite, the primary neon on white (motion/Tactile).
    val tactileLight = if (darkTheme) Color.White else colorScheme.primary
    // Components we do not wrap (date picker, snackbar) keep a faint ripple of the primary.
    val ripple = RippleConfiguration(color = colorScheme.primary, rippleAlpha = RippleAlpha(0.10f, 0.10f, 0.06f, 0.12f))
    CompositionLocalProvider(
        LocalAmountPalette provides amounts,
        LocalStatusPalette provides status,
        LocalDarkTheme provides darkTheme,
        LocalHeroPalette provides hero,
        LocalPanelColor provides colors.panel,
        LocalTactileLight provides tactileLight,
        LocalRippleConfiguration provides ripple,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            motionScheme = CoShellMotionScheme,
            typography = CoShellTypography,
            shapes = CoShellShapes,
            content = content,
        )
    }
}

/** Everything a palette paints in one theme; pure, so `ColorContrastTest` checks every palette. */
internal data class ThemeColors(
    val scheme: ColorScheme,
    val amounts: AmountPalette,
    val status: StatusPalette,
    val hero: HeroPalette,
    val heroStops: List<Color>,
    /** Top and bottom capsules — [panelColor]. */
    val panel: Color,
)

internal fun themeColorsFor(palette: CoShellPalette, dark: Boolean): ThemeColors {
    val p = palette.spec
    return if (dark) {
        val scheme = darkSchemeOf(p)
        val stops = heroDarkStops(p)
        ThemeColors(
            scheme = scheme,
            amounts = AmountPalette(p.incomeDark, p.expenseDark, scheme.onSurfaceVariant),
            status = StatusPalette(p.warningDark, p.incomeDark),
            hero = HeroPalette(
                brush = Brush.linearGradient(stops),
                content = scheme.onSurface,
                amount = readableOn(p.primary, stops, Color.White),
                hint = readableOn(lerp(Color.White, p.backgroundDark, HERO_HINT_DARK), stops, Color.White),
                track = HERO_TRACK_DARK,
                expense = readableOn(lerp(p.expenseDark, Color.White, HERO_ALERT_SHIFT_DARK), stops, Color.White),
                warning = readableOn(p.warningDark, stops, Color.White),
            ),
            heroStops = stops,
            panel = panelOf(p, dark = true, scheme = scheme),
        )
    } else {
        val scheme = lightSchemeOf(p)
        val stops = heroLightStops(p)
        ThemeColors(
            scheme = scheme,
            amounts = AmountPalette(p.incomeLight, p.expenseLight, scheme.onSurfaceVariant),
            status = StatusPalette(p.warningLight, p.incomeLight),
            hero = HeroPalette(
                brush = Brush.linearGradient(stops),
                content = scheme.onSurface,
                amount = scheme.onSurface,
                hint = readableOn(scheme.onSurfaceVariant, stops, scheme.onSurface),
                track = scheme.onSurface.copy(alpha = HERO_TRACK_LIGHT_ALPHA),
                expense = readableOn(lerp(p.expenseLight, Color.Black, HERO_ALERT_SHIFT_LIGHT), stops, Color.Black),
                warning = readableOn(lerp(p.warningLight, Color.Black, HERO_ALERT_SHIFT_LIGHT), stops, Color.Black),
            ),
            heroStops = stops,
            panel = panelOf(p, dark = false, scheme = scheme),
        )
    }
}

/** Dark hero gradient (variant Б): the neon, income and accent of the palette sunk into its graphite background. */
internal fun heroDarkStops(p: PaletteSpec): List<Color> = listOf(
    lerp(p.backgroundDark, p.primary, 0.30f),
    lerp(p.backgroundDark, p.incomeDark, 0.16f),
    lerp(p.backgroundDark, p.accentDark, 0.24f),
)

/**
 * Light hero gradient. Neon themes (#493 Б, owner's poll 2026-11-23): a saturated gradient up to the pure neon. Soft themes keep the
 * gentle one (owner 2026-11-16, «Б»): the primary, a lighter primary, a light accent. Dark ink reads on each stop (`ColorContrastTest`).
 */
internal fun heroLightStops(p: PaletteSpec): List<Color> =
    if (p.soft) listOf(p.primary, lerp(p.primary, Color.White, 0.3f), lerp(p.accentDark, Color.White, 0.2f))
    else listOf(lerp(p.primary, Color.White, 0.45f), lerp(p.primary, Color.White, 0.15f), p.primary)

/**
 * [color] moved towards [toward] in 5 % steps until it reads (≥ 4.6:1) on every one of [backgrounds] — the neon amount
 * on the dark hero gradient of Гранат, Эспрессо, Василёк. Same rule as `readable_on` in scripts/agent/themes_1_0.py.
 */
internal fun readableOn(color: Color, backgrounds: List<Color>, toward: Color): Color {
    var k = 0f
    var c = color
    while (backgrounds.minOf { contrastRatio(c, it) } < READABLE && k < 1f) {
        k += READABLE_STEP
        c = lerp(color, toward, k)
    }
    return c
}

internal fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance() + 0.05f
    val lb = b.luminance() + 0.05f
    return maxOf(la, lb) / minOf(la, lb)
}

private const val READABLE = 4.6f
private const val READABLE_STEP = 0.05f

private val HERO_TRACK_DARK = Color.White.copy(alpha = 0.16f)
private const val HERO_TRACK_LIGHT_ALPHA = 0.14f
private const val HERO_HINT_DARK = 0.28f // the theme's grey (0.38) is too dim on the neon stop
private const val HERO_ALERT_SHIFT_DARK = 0.4f
private const val HERO_ALERT_SHIFT_LIGHT = 0.2f

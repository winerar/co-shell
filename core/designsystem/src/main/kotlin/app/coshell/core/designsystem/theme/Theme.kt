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
import app.coshell.core.designsystem.motion.LocalTactileLight
import app.coshell.core.designsystem.tokens.CoShellShapes

/** Amount colors: income, expense (always with "−" too), transfer (neutral). */
@Immutable
data class AmountPalette(val income: Color, val expense: Color, val transfer: Color)

/** Colors for states Material has no slot for. */
@Immutable
data class StatusPalette(val warning: Color, val success: Color)

val LocalAmountPalette = staticCompositionLocalOf { themeColorsFor(CoShellPalette.Volt, dark = false).amounts }

val LocalStatusPalette = staticCompositionLocalOf { themeColorsFor(CoShellPalette.Volt, dark = false).status }

/**
 * The main block of a screen («Обзор» total, budgets month, envelope head — `CoShellCard(accent = true)`), filled
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

val LocalHeroPalette = staticCompositionLocalOf { themeColorsFor(CoShellPalette.Volt, dark = false).hero }

/** Whether [CoShellTheme] is dark — e.g. to pick a category color's variant. */
val LocalDarkTheme = staticCompositionLocalOf { false }

/**
 * App theme: one of the own neon [CoShellPalette]s (default — acid [CoShellPalette.Volt]), light and dark.
 * Wallpaper colors (Material You) are not used — the app looks the same on both family phones. [darkTheme] comes from the theme setting
 * (system / light / dark).
 */
@Composable
fun CoShellTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: CoShellPalette = CoShellPalette.Volt,
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
)

internal fun themeColorsFor(palette: CoShellPalette, dark: Boolean): ThemeColors {
    val p = palette.spec
    return if (dark) {
        val scheme = darkSchemeOf(p)
        val stops = heroDarkStops(p)
        ThemeColors(
            scheme = scheme,
            amounts = AmountPalette(p.incomeDark, p.expenseDark, scheme.onSurfaceVariant),
            status = StatusPalette(StatusColors.WarningDark, p.incomeDark),
            hero = HeroPalette(
                brush = Brush.linearGradient(stops),
                content = scheme.onSurface,
                amount = scheme.primary,
                hint = lerp(Color.White, p.backgroundDark, HERO_HINT_DARK),
                track = HERO_TRACK_DARK,
                expense = lerp(p.expenseDark, Color.White, HERO_ALERT_SHIFT_DARK),
                warning = StatusColors.WarningDark,
            ),
            heroStops = stops,
        )
    } else {
        val scheme = lightSchemeOf(p)
        ThemeColors(
            scheme = scheme,
            amounts = AmountPalette(p.incomeLight, p.expenseLight, scheme.onSurfaceVariant),
            status = StatusPalette(StatusColors.WarningLight, p.incomeLight),
            hero = HeroPalette(
                brush = Brush.linearGradient(p.heroLight),
                content = scheme.onSurface,
                amount = scheme.onSurface,
                hint = scheme.onSurfaceVariant,
                track = scheme.onSurface.copy(alpha = HERO_TRACK_LIGHT_ALPHA),
                expense = lerp(p.expenseLight, Color.Black, HERO_ALERT_SHIFT_LIGHT),
                warning = lerp(StatusColors.WarningLight, Color.Black, HERO_ALERT_SHIFT_LIGHT),
            ),
            heroStops = p.heroLight,
        )
    }
}

/** Dark hero gradient (variant Б): the neon, income and accent of the palette sunk into its graphite background. */
internal fun heroDarkStops(p: PaletteSpec): List<Color> = listOf(
    lerp(p.backgroundDark, p.primary, 0.30f),
    lerp(p.backgroundDark, p.incomeDark, 0.16f),
    lerp(p.backgroundDark, p.accentDark, 0.24f),
)

private val HERO_TRACK_DARK = Color.White.copy(alpha = 0.16f)
private const val HERO_TRACK_LIGHT_ALPHA = 0.14f
private const val HERO_HINT_DARK = 0.28f // the theme's grey (0.38) is too dim on the neon stop
private const val HERO_ALERT_SHIFT_DARK = 0.4f
private const val HERO_ALERT_SHIFT_LIGHT = 0.2f

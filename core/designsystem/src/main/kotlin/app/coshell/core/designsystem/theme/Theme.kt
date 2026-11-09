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
 * The main block of a screen («Обзор» total, `CoShellCard(accent = true)`). Light theme: a neon gradient of
 * the palette (issue #62 — "the card from variant C") with dark ink on it; dark theme: no fill
 * ([brush] = null), the card keeps its primary-coloured border.
 */
@Immutable
data class HeroPalette(val brush: Brush?, val content: Color, val amount: Color)

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
        ThemeColors(
            scheme = scheme,
            amounts = AmountPalette(p.incomeDark, p.expenseDark, scheme.onSurfaceVariant),
            status = StatusPalette(StatusColors.WarningDark, p.incomeDark),
            hero = HeroPalette(null, scheme.onSurface, scheme.primary),
            heroStops = emptyList(),
        )
    } else {
        val scheme = lightSchemeOf(p)
        ThemeColors(
            scheme = scheme,
            amounts = AmountPalette(p.incomeLight, p.expenseLight, scheme.onSurfaceVariant),
            status = StatusPalette(StatusColors.WarningLight, p.incomeLight),
            hero = HeroPalette(Brush.linearGradient(p.heroLight), scheme.onSurface, scheme.onSurface),
            heroStops = p.heroLight,
        )
    }
}

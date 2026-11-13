package app.coshell.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Light theme of a palette: tinted white background, the neon primary as a fill with dark ink (buttons,
 * selected tab, FAB, switches, selected day); selected chips and segments sit on a pale wash of the accent
 * (`secondaryContainer`). Neon on white is unreadable as text, so text accents take [accentInk] — the ink.
 */
internal fun lightSchemeOf(p: PaletteSpec): ColorScheme {
    val n = lightNeutrals(p.backgroundLight)
    return lightColorScheme(
        primary = p.primary,
        onPrimary = p.onPrimary,
        primaryContainer = p.primary,
        onPrimaryContainer = p.onPrimary,
        inversePrimary = p.primary,
        secondary = p.accentLight,
        onSecondary = Color.White,
        secondaryContainer = lerp(p.accentDark, Color.White, 0.78f),
        onSecondaryContainer = lerp(p.accentLight, Color.Black, 0.55f),
        tertiary = p.tertiaryLight,
        onTertiary = Color.White,
        tertiaryContainer = lerp(p.tertiaryDark, Color.White, 0.72f),
        onTertiaryContainer = lerp(p.tertiaryLight, Color.Black, 0.55f),
        error = p.expenseLight,
        onError = Color.White,
        errorContainer = CoralPale,
        onErrorContainer = CoralInk,
        background = n.background,
        onBackground = n.text,
        surface = n.background,
        onSurface = n.text,
        surfaceVariant = n.high,
        onSurfaceVariant = n.textMuted,
        surfaceTint = p.primary,
        inverseSurface = darkNeutrals(p.backgroundDark).high,
        inverseOnSurface = darkNeutrals(p.backgroundDark).text,
        outline = n.line,
        outlineVariant = n.lineSoft,
        surfaceBright = n.bright,
        surfaceDim = n.highest,
        surfaceContainerLowest = n.lowest,
        surfaceContainerLow = n.low,
        surfaceContainer = n.container,
        surfaceContainerHigh = n.high,
        surfaceContainerHighest = n.highest,
    )
}

/** Dark theme of a palette: tinted graphite, neon accents as is (the primary fills with dark ink), no glow. */
internal fun darkSchemeOf(p: PaletteSpec): ColorScheme {
    val n = darkNeutrals(p.backgroundDark)
    return darkColorScheme(
        primary = p.primary,
        onPrimary = p.onPrimary,
        primaryContainer = p.primary,
        onPrimaryContainer = p.onPrimary,
        inversePrimary = lightNeutrals(p.backgroundLight).text,
        secondary = p.accentDark,
        onSecondary = lerp(p.accentDark, Color.Black, 0.82f),
        secondaryContainer = lerp(p.accentDark, n.background, 0.68f),
        onSecondaryContainer = lerp(p.accentDark, Color.White, 0.75f),
        tertiary = p.tertiaryDark,
        onTertiary = lerp(p.tertiaryDark, Color.Black, 0.82f),
        tertiaryContainer = lerp(p.tertiaryDark, n.background, 0.7f),
        onTertiaryContainer = lerp(p.tertiaryDark, Color.White, 0.75f),
        error = p.expenseDark,
        onError = lerp(p.expenseDark, Color.Black, 0.85f),
        errorContainer = CoralShadow,
        onErrorContainer = CoralPale,
        background = n.background,
        onBackground = n.text,
        surface = n.background,
        onSurface = n.text,
        surfaceVariant = n.high,
        onSurfaceVariant = n.textMuted,
        surfaceTint = p.primary,
        inverseSurface = n.text,
        inverseOnSurface = n.high,
        outline = n.line,
        outlineVariant = n.lineSoft,
        surfaceBright = n.bright,
        surfaceDim = n.background,
        surfaceContainerLowest = n.lowest,
        surfaceContainerLow = n.low,
        surfaceContainer = n.container,
        surfaceContainerHigh = n.high,
        surfaceContainerHighest = n.highest,
    )
}

/** The default palette's schemes — previews and tests that do not care about the palette. */
internal val CoShellLightColors = lightSchemeOf(CoShellPalette.Lime.spec)
internal val CoShellDarkColors = darkSchemeOf(CoShellPalette.Lime.spec)

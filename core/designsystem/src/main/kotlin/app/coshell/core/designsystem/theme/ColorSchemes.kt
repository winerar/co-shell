package app.coshell.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Light theme (issue #62, variant B): mint-tinted white background, Neon Mint fills with dark ink (buttons,
 * selected tab, FAB, switches, selected day), acid lime as the second accent (Material puts
 * `secondaryContainer` under selected chips and segments). Neon on white is unreadable as text, so
 * text accents (links, headers, text buttons, focused fields) take [accentInk] — the ink, not a deep green.
 */
internal val CoShellLightColors = lightColorScheme(
    primary = NeonMint,
    onPrimary = MintInk,
    primaryContainer = NeonMint,
    onPrimaryContainer = MintInk,
    inversePrimary = NeonMint,
    secondary = LimeAcid,
    onSecondary = LimeInk,
    secondaryContainer = LimeAcid,
    onSecondaryContainer = LimeInk,
    tertiary = SkyDeep,
    onTertiary = Color.White,
    tertiaryContainer = SkyPale,
    onTertiaryContainer = SkyInk,
    error = AmountColors.ExpenseLight,
    onError = Color.White,
    errorContainer = CoralPale,
    onErrorContainer = CoralInk,
    background = Frost1,
    onBackground = FrostText,
    surface = Frost1,
    onSurface = FrostText,
    surfaceVariant = Frost4,
    onSurfaceVariant = FrostTextMuted,
    surfaceTint = NeonMint,
    inverseSurface = Graphite4,
    inverseOnSurface = GraphiteText,
    outline = FrostLine,
    outlineVariant = FrostLineSoft,
    surfaceBright = Frost0,
    surfaceDim = Frost5,
    surfaceContainerLowest = Frost0,
    surfaceContainerLow = Frost2,
    surfaceContainer = Frost3,
    surfaceContainerHigh = Frost4,
    surfaceContainerHighest = Frost5,
)

/** Dark theme: graphite background, neon accents as is (Neon Mint fills with dark ink), no glow effects. */
internal val CoShellDarkColors = darkColorScheme(
    primary = NeonMint,
    onPrimary = MintInk,
    primaryContainer = NeonMint,
    onPrimaryContainer = MintInk,
    inversePrimary = FrostText,
    secondary = VioletNeon,
    onSecondary = VioletInk,
    secondaryContainer = VioletShadow,
    onSecondaryContainer = VioletPale,
    tertiary = SkyNeon,
    onTertiary = SkyInk,
    tertiaryContainer = SkyShadow,
    onTertiaryContainer = SkyPale,
    error = CoralNeon,
    onError = CoralInk,
    errorContainer = CoralShadow,
    onErrorContainer = CoralPale,
    background = Graphite1,
    onBackground = GraphiteText,
    surface = Graphite1,
    onSurface = GraphiteText,
    surfaceVariant = Graphite4,
    onSurfaceVariant = GraphiteTextMuted,
    surfaceTint = NeonMint,
    inverseSurface = GraphiteText,
    inverseOnSurface = Graphite4,
    outline = GraphiteLine,
    outlineVariant = GraphiteLineSoft,
    surfaceBright = Graphite5,
    surfaceDim = Graphite1,
    surfaceContainerLowest = Graphite0,
    surfaceContainerLow = Graphite2,
    surfaceContainer = Graphite3,
    surfaceContainerHigh = Graphite4,
    surfaceContainerHighest = Graphite5,
)

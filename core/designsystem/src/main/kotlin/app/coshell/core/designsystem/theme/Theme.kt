package app.coshell.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import app.coshell.core.designsystem.motion.LocalTactileLight
import app.coshell.core.designsystem.tokens.CoShellShapes

/** Amount colors: income, expense (always with "−" too), transfer (neutral). */
@Immutable
data class AmountPalette(val income: Color, val expense: Color, val transfer: Color)

/** Colors for states Material has no slot for. */
@Immutable
data class StatusPalette(val warning: Color, val success: Color)

val LocalAmountPalette = staticCompositionLocalOf {
    AmountPalette(AmountColors.IncomeLight, AmountColors.ExpenseLight, CoShellLightColors.onSurfaceVariant)
}

val LocalStatusPalette = staticCompositionLocalOf {
    StatusPalette(StatusColors.WarningLight, StatusColors.SuccessLight)
}

/** Whether [CoShellTheme] is dark — e.g. to pick a category color's variant. */
val LocalDarkTheme = staticCompositionLocalOf { false }

/**
 * App theme: own neon palette, light and dark. Wallpaper colors (Material You) are not used —
 * the app looks the same on both family phones. [darkTheme] comes from the theme setting
 * (system / light / dark).
 */
@Composable
fun CoShellTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) CoShellDarkColors else CoShellLightColors
    val amounts = if (darkTheme) {
        AmountPalette(AmountColors.IncomeDark, AmountColors.ExpenseDark, colorScheme.onSurfaceVariant)
    } else {
        AmountPalette(AmountColors.IncomeLight, AmountColors.ExpenseLight, colorScheme.onSurfaceVariant)
    }
    val status = if (darkTheme) {
        StatusPalette(StatusColors.WarningDark, StatusColors.SuccessDark)
    } else {
        StatusPalette(StatusColors.WarningLight, StatusColors.SuccessLight)
    }

    // Pressed elements catch light: white on graphite, mint on white (motion/Tactile).
    val tactileLight = if (darkTheme) Color.White else colorScheme.primary
    // Components we do not wrap (date picker, snackbar) keep a faint mint ripple.
    val ripple = RippleConfiguration(color = colorScheme.primary, rippleAlpha = RippleAlpha(0.10f, 0.10f, 0.06f, 0.12f))
    CompositionLocalProvider(
        LocalAmountPalette provides amounts,
        LocalStatusPalette provides status,
        LocalDarkTheme provides darkTheme,
        LocalTactileLight provides tactileLight,
        LocalRippleConfiguration provides ripple,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CoShellTypography,
            shapes = CoShellShapes,
            content = content,
        )
    }
}

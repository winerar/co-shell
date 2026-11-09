package app.coshell.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import app.coshell.core.designsystem.tokens.CoShellShapes

private val LightColors = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    primaryContainer = Teal90,
    onPrimaryContainer = Teal10,
    secondary = Sand40,
    onSecondary = Color.White,
    secondaryContainer = Sand90,
    onSecondaryContainer = Sand10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
)

private val DarkColors = darkColorScheme(
    primary = Teal80,
    onPrimary = Teal20,
    primaryContainer = Teal30,
    onPrimaryContainer = Teal90,
    secondary = Sand80,
    onSecondary = Sand20,
    secondaryContainer = Sand30,
    onSecondaryContainer = Sand90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
)

@Immutable
data class AmountPalette(val income: Color, val expense: Color)

val LocalAmountPalette = staticCompositionLocalOf {
    AmountPalette(income = AmountColors.IncomeLight, expense = AmountColors.ExpenseLight)
}

/**
 * App theme. Dynamic color (Material You) is off by default so the app looks the same
 * on both family phones; it can be enabled from settings later.
 */
@Composable
fun CoShellTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    val amountPalette = if (darkTheme) {
        AmountPalette(income = AmountColors.IncomeDark, expense = AmountColors.ExpenseDark)
    } else {
        AmountPalette(income = AmountColors.IncomeLight, expense = AmountColors.ExpenseLight)
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalAmountPalette provides amountPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CoShellTypography,
            shapes = CoShellShapes,
            content = content,
        )
    }
}

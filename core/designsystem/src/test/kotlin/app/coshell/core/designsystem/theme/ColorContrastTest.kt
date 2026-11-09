package app.coshell.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

/** WCAG contrast of the neon palette: text ≥ 4.5, icons and large amounts ≥ 3. */
class ColorContrastTest {
    private val schemes = mapOf("light" to CoShellLightColors, "dark" to CoShellDarkColors)

    @Test
    fun textOnItsBackgroundIsReadable() = schemes.forEach { (name, s) ->
        pairs(s).forEach { (label, fg, bg) -> assertContrast("$name $label", fg, bg, 4.5) }
    }

    @Test
    fun amountsAreReadableOnBothThemes() {
        val light = CoShellLightColors.background
        val dark = CoShellDarkColors.background
        assertContrast("light income", AmountColors.IncomeLight, light, 4.5)
        assertContrast("light expense", AmountColors.ExpenseLight, light, 4.5)
        assertContrast("dark income", AmountColors.IncomeDark, dark, 4.5)
        assertContrast("dark expense", AmountColors.ExpenseDark, dark, 4.5)
        assertContrast("light warning", StatusColors.WarningLight, light, 4.5)
        assertContrast("dark warning", StatusColors.WarningDark, dark, 4.5)
    }

    @Test
    fun heroCardTextIsReadableOnTheWholeGradient() {
        // Light theme hero card: ink text over every stop of the mint → lime gradient; grey hints too.
        listOf(MintAcid, LimeSoft, LimeAcid).forEach { stop ->
            assertContrast("hero ink on $stop", MintInk, stop, 4.5)
            assertContrast("hero hint on $stop", CoShellLightColors.onSurfaceVariant, stop, 4.5)
        }
    }

    private fun pairs(s: ColorScheme) = listOf(
        Triple("onBackground", s.onBackground, s.background),
        Triple("onSurfaceVariant", s.onSurfaceVariant, s.surfaceContainerHigh),
        Triple("onPrimary", s.onPrimary, s.primary),
        Triple("primary on surface", s.primary, s.surface),
        Triple("onPrimaryContainer", s.onPrimaryContainer, s.primaryContainer),
        Triple("onSecondary", s.onSecondary, s.secondary),
        Triple("secondary on surface", s.secondary, s.surface),
        Triple("onSecondaryContainer", s.onSecondaryContainer, s.secondaryContainer),
        Triple("onTertiary", s.onTertiary, s.tertiary),
        Triple("onTertiaryContainer", s.onTertiaryContainer, s.tertiaryContainer),
        Triple("onError", s.onError, s.error),
        Triple("error on surface", s.error, s.surface),
        Triple("onErrorContainer", s.onErrorContainer, s.errorContainer),
        Triple("inverseOnSurface", s.inverseOnSurface, s.inverseSurface),
    )
}

internal fun contrast(a: Color, b: Color): Double {
    val (hi, lo) = listOf(a.luminance() + 0.05, b.luminance() + 0.05).sortedDescending()
    return (hi / lo).toDouble()
}

internal fun assertContrast(label: String, fg: Color, bg: Color, min: Double) {
    val ratio = contrast(fg, bg)
    assertTrue("$label: contrast %.2f < $min".format(ratio), ratio >= min)
}

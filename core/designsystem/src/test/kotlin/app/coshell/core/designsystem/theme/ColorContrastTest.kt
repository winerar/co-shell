package app.coshell.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** WCAG contrast of every neon palette: text ≥ 4.5, icons and large amounts ≥ 3. */
class ColorContrastTest {
    private val themes = CoShellPalette.entries.flatMap { palette ->
        listOf(false, true).map { dark -> "$palette ${if (dark) "dark" else "light"}" to themeColorsFor(palette, dark) }
    }

    @Test
    fun textOnItsBackgroundIsReadable() = themes.forEach { (name, t) ->
        val s = t.scheme
        pairs(s).forEach { (label, fg, bg) -> assertContrast("$name $label", fg, bg, 4.5) }
        assertContrast("$name accent ink on surface", accentInkFor(s, name.endsWith("dark")), s.surface, 4.5)
    }

    @Test
    fun progressIsVisibleOnItsTrack() = themes.forEach { (name, t) ->
        // Graphite-neon progress (owner's choice 2026-11-09): the moving part stands out (≥ 3, like icons).
        val p = progressColorsFor(t.scheme, name.endsWith("dark"))
        assertContrast("$name progress on track", p.indicator, p.track, 3.0)
    }

    @Test
    fun darkThemeNeonIsReadableAsText() = themes.filter { it.first.endsWith("dark") }.forEach { (name, t) ->
        assertContrast("$name primary on surface", t.scheme.primary, t.scheme.surface, 4.5)
        assertContrast("$name secondary on surface", t.scheme.secondary, t.scheme.surface, 4.5)
    }

    @Test
    fun voltIsTheAcidDefault() {
        // Owner's choice 2026-11-09: palette A+ «Кислота» (#B8FF2E) is the default, a fill with dark ink.
        assertEquals(Color(0xFFB8FF2E), CoShellLightColors.primary)
        assertEquals(Color(0xFFB8FF2E), CoShellDarkColors.primaryContainer)
    }

    @Test
    fun amountsAreReadableOnTheirBackground() = themes.forEach { (name, t) ->
        val bg = t.scheme.background
        assertContrast("$name income", t.amounts.income, bg, 4.5)
        assertContrast("$name expense", t.amounts.expense, bg, 4.5)
        assertContrast("$name warning", t.status.warning, bg, 4.5)
    }

    @Test
    fun incomeIsNotThePrimary() = themes.forEach { (name, t) ->
        // Income must not blend with the neon primary (owner, palette A: green income was indistinguishable).
        assertTrue("$name income = primary", t.amounts.income != t.scheme.primary)
    }

    @Test
    fun heroCardTextIsReadableOnTheWholeGradient() = themes.forEach { (name, t) ->
        // Light theme hero card: ink text and grey hints over every stop of the palette gradient.
        t.heroStops.forEach { stop ->
            assertContrast("$name hero ink on $stop", t.hero.content, stop, 4.5)
            assertContrast("$name hero hint on $stop", t.scheme.onSurfaceVariant, stop, 4.5)
        }
    }

    private fun pairs(s: ColorScheme) = listOf(
        Triple("onBackground", s.onBackground, s.background),
        Triple("onSurfaceVariant", s.onSurfaceVariant, s.surfaceContainerHigh),
        Triple("onPrimary", s.onPrimary, s.primary),
        Triple("onPrimaryContainer", s.onPrimaryContainer, s.primaryContainer),
        Triple("onSecondary", s.onSecondary, s.secondary),
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

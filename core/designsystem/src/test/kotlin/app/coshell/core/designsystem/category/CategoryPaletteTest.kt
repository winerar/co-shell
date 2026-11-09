package app.coshell.core.designsystem.category

import androidx.compose.ui.graphics.Color
import app.coshell.core.designsystem.theme.CoShellDarkColors
import app.coshell.core.designsystem.theme.CoShellLightColors
import app.coshell.core.designsystem.theme.assertContrast
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryPaletteTest {
    @Test
    fun eighteenDistinctColours() {
        assertEquals(18, CategoryPalette.colors.size)
        assertEquals(18, CategoryPalette.colors.toSet().size)
    }

    @Test
    fun iconsAreVisibleOnBothThemes() = CategoryPalette.swatches.forEach { s ->
        // Icon tint over its 16 % tinted circle ≈ over the background; 3:1 for icons (WCAG 1.4.11).
        assertContrast("light %X".format(s.light), Color(s.light), CoShellLightColors.surface, 3.0)
        assertContrast("dark %X".format(s.stored), Color(s.stored), CoShellDarkColors.surface, 3.0)
    }

    @Test
    fun displayPicksTheShadeForTheTheme() {
        val mint = CategoryPalette.swatches[7]
        assertEquals(mint.stored, CategoryPalette.display(mint.stored, dark = true))
        assertEquals(mint.light, CategoryPalette.display(mint.stored, dark = false))
    }

    @Test
    fun preNeonColoursMapToTheSwatchInTheSamePlace() {
        val oldGreen = 0xFF4CAF50
        val green = CategoryPalette.swatches[8]
        assertEquals(green.stored, CategoryPalette.normalize(oldGreen))
        assertEquals(green.light, CategoryPalette.display(oldGreen, dark = false))
        assertEquals(green.stored, CategoryPalette.display(oldGreen, dark = true))
    }

    @Test
    fun foreignColoursAreKeptAsIs() {
        val imported = 0xFF123456
        assertEquals(imported, CategoryPalette.normalize(imported))
        assertEquals(imported, CategoryPalette.display(imported, dark = false))
    }
}

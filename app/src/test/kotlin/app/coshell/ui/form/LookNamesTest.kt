package app.coshell.ui.form

import app.coshell.core.designsystem.category.CategoryIcons
import app.coshell.core.designsystem.category.CategoryPalette
import org.junit.Assert.assertEquals
import org.junit.Test

class LookNamesTest {
    @Test fun `every palette colour has a spoken name`() {
        assertEquals(CategoryPalette.colors.size, COLOR_NAMES.size)
        assertEquals("names are distinct", COLOR_NAMES.size, COLOR_NAMES.toSet().size)
    }

    @Test fun `every category icon has a spoken name`() {
        assertEquals(CategoryIcons.keys.toSet(), ICON_NAMES.keys)
        assertEquals("names are distinct", ICON_NAMES.size, ICON_NAMES.values.toSet().size)
    }
}

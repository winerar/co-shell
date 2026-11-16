package app.coshell.core.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemeModeTest {
    @Test
    fun systemFollowsThePhone() {
        assertTrue(ThemeMode.SYSTEM.isDark(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemDark = false))
    }

    @Test
    fun explicitModesIgnoreThePhone() {
        assertFalse(ThemeMode.LIGHT.isDark(systemDark = true))
        assertTrue(ThemeMode.DARK.isDark(systemDark = false))
    }

    @Test
    fun parseFallsBackToSystem() {
        assertEquals(ThemeMode.DARK, ThemeMode.parse("DARK"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.parse(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.parse("purple"))
    }

    @Test
    fun paletteParseFallsBackToLime() {
        assertEquals(ThemePalette.KVAS, ThemePalette.parse("KVAS"))
        assertEquals(ThemePalette.LOTUS, ThemePalette.parse("LOTUS"))
        assertEquals(ThemePalette.LIME, ThemePalette.parse(null))
        assertEquals(ThemePalette.LIME, ThemePalette.parse("MINT"))
    }

    @Test
    fun palettesOf0xMoveToTheNearestTheme() {
        // 1.0 (#354): 14 fruit palettes → 9 themes; the names before the fruits (#271) too.
        assertEquals(ThemePalette.LOTUS, ThemePalette.parse("PASSIONFRUIT"))
        assertEquals(ThemePalette.KVAS, ThemePalette.parse("MANGO"))
        assertEquals(ThemePalette.CORNFLOWER, ThemePalette.parse("BILBERRY"))
        assertEquals(ThemePalette.SAKURA, ThemePalette.parse("PITAYA"))
        assertEquals(ThemePalette.BANANA, ThemePalette.parse("LEMON"))
        assertEquals(ThemePalette.ESPRESSO, ThemePalette.parse("PEACH"))
        assertEquals(ThemePalette.MATE, ThemePalette.parse("PEAR"))
        assertEquals(ThemePalette.LIME, ThemePalette.parse("VOLT"))
        assertEquals(ThemePalette.LOTUS, ThemePalette.parse("MOJITO"))
    }

    @Test
    fun nineThemesInOneList() {
        assertEquals(9, ThemePalette.entries.size)
        assertEquals(ThemePalette.LIME, ThemePalette.entries.first())
    }
}

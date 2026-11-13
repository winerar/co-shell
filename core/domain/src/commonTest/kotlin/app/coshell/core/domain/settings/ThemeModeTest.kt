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
        assertEquals(ThemePalette.MANGO, ThemePalette.parse("MANGO"))
        assertEquals(ThemePalette.PEACH, ThemePalette.parse("PEACH"))
        assertEquals(ThemePalette.LIME, ThemePalette.parse(null))
        assertEquals(ThemePalette.LIME, ThemePalette.parse("MINT"))
    }

    @Test
    fun paletteSavedBeforeFruitNamesIsKept() {
        // #271: the phone keeps its choice after the rename; Мохито became the violet Маракуйя.
        assertEquals(ThemePalette.LIME, ThemePalette.parse("VOLT"))
        assertEquals(ThemePalette.PASSIONFRUIT, ThemePalette.parse("MOJITO"))
        assertEquals(ThemePalette.BILBERRY, ThemePalette.parse("ULTRA"))
        assertEquals(ThemePalette.FEIJOA, ThemePalette.parse("LAGOON"))
        assertEquals(ThemePalette.PITAYA, ThemePalette.parse("SAKURA"))
    }

    @Test
    fun sevenJuicyAndSevenSoft() {
        val groups = ThemePalette.entries.groupBy { it.group }
        assertEquals(7, groups.getValue(PaletteGroup.JUICY).size)
        assertEquals(7, groups.getValue(PaletteGroup.SOFT).size)
    }
}

package app.coshell.core.designsystem.components

import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.designsystem.theme.assertContrast
import app.coshell.core.designsystem.theme.themeColorsFor
import org.junit.Assert.assertEquals
import org.junit.Test

/** [CardTone.Story] (#554, poll 2026-11-24 «А»): the hero's text and hints stay readable on every stop of the soft gradient. */
class StoryToneTest {
    @Test
    fun storyTextIsReadableOnEveryStop() = CoShellPalette.entries.forEach { palette ->
        listOf(false, true).forEach { dark ->
            val t = themeColorsFor(palette, dark)
            val name = "$palette ${if (dark) "dark" else "light"}"
            storyStops(t.scheme, t.hero).forEachIndexed { i, stop ->
                assertContrast("$name story ink on stop $i", t.hero.content, stop, 4.5)
                assertContrast("$name story hint on stop $i", t.hero.hint, stop, 4.5)
            }
        }
    }

    @Test
    fun storyFadesFromTheThemeColourToTheCard() {
        val t = themeColorsFor(CoShellPalette.Lime, dark = false)
        val s = t.scheme
        val stops = storyStops(s, t.hero)
        assertEquals(3, stops.size)
        assertEquals("the bottom is almost the card", true, stops.last() != s.surface && stops.first() != s.primary)
    }
}

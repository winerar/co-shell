package app.coshell.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The light theme of the owner's poll 2026-11-23 (#493 Б, нежные — А): deep palette inks, neon on large areas only for neon themes. */
class LightThemeInksTest {
    private val soft = setOf(CoShellPalette.Mate, CoShellPalette.Sakura, CoShellPalette.Lotus)

    @Test
    fun `only Mate, Sakura and Lotus are soft`() {
        assertEquals(soft, CoShellPalette.entries.filter { it.spec.soft }.toSet())
    }

    @Test
    fun `accent ink in the light theme is the palette's deep shade, not black`() {
        CoShellPalette.entries.forEach { p ->
            val s = themeColorsFor(p, dark = false).scheme
            assertEquals("$p", p.spec.inkLight, accentInkFor(s, dark = false))
            assertTrue("$p ink must read on the card", contrastRatio(p.spec.inkLight, s.surface) >= 4.5f)
            assertNotEquals("$p body text is tinted", Color.Black, s.onSurface)
        }
    }

    @Test
    fun `neon themes get a primary-toned capsule and a pure neon hero end, soft themes keep theirs`() {
        CoShellPalette.entries.forEach { p ->
            val t = themeColorsFor(p, dark = false)
            if (p in soft) {
                assertEquals("$p panel", t.scheme.surfaceContainerHigh, t.panel)
                assertEquals("$p hero starts at the primary", p.spec.primary, t.heroStops.first())
            } else {
                assertNotEquals("$p panel", t.scheme.surfaceContainerHigh, t.panel)
                assertEquals("$p hero ends in the pure neon", p.spec.primary, t.heroStops.last())
            }
        }
    }

    @Test
    fun `neon cards carry more of the primary than soft ones`() {
        assertTrue(contrastRatio(Color.White, themeColorsFor(CoShellPalette.Garnet, false).scheme.surface) >
            contrastRatio(Color.White, themeColorsFor(CoShellPalette.Sakura, false).scheme.surface))
    }

    @Test
    fun `the dark theme keeps its capsule`() {
        CoShellPalette.entries.forEach { p ->
            val t = themeColorsFor(p, dark = true)
            assertEquals("$p", t.scheme.surfaceContainerHigh, t.panel)
        }
    }
}

package app.coshell.core.designsystem.category

import androidx.compose.ui.graphics.Color
import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.designsystem.theme.assertContrast
import app.coshell.core.designsystem.theme.contrast
import app.coshell.core.designsystem.theme.themeColorsFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** The 24 icon colours of 1.0 (#105, owner 2026-11-16): distinct, readable, rings for pale fills in every theme. */
class CategoryPaletteTest {
    @Test
    fun twentyFourDistinctColours() {
        assertEquals(24, CategoryPalette.colors.size)
        assertEquals(24, CategoryPalette.colors.toSet().size)
    }

    @Test
    fun everyTwoColoursAreFarApart() {
        // CIEDE2000 ≥ 16 (owner: v1 at 11.5 looked alike in the light theme; 10 is the usual «distinct side by side»).
        val c = CategoryPalette.colors
        for (i in c.indices) for (j in i + 1 until c.size) {
            val d = deltaE2000(Color(c[i]), Color(c[j]))
            assertTrue("%X / %X: ΔE %.1f < 16".format(c[i], c[j], d), d >= 16.0)
        }
    }

    @Test
    fun glyphAndInitialsReadOnEveryColour() = CategoryPalette.colors.forEach { c ->
        val fill = Color(c)
        assertContrast("ink on %X".format(c), inkOn(fill), fill, 4.5)
    }

    @Test
    fun everyBadgeStandsOutOnEveryThemeSurface() = CoShellPalette.entries.forEach { palette ->
        listOf(false, true).forEach { dark ->
            val s = themeColorsFor(palette, dark).scheme
            listOf(s.surface, s.background, s.surfaceContainer).forEach { bg ->
                CategoryPalette.colors.forEach { c ->
                    val fill = Color(c)
                    val edge = CategoryPalette.ring(fill, bg) ?: fill
                    assertContrast("$palette dark=$dark %X edge".format(c), edge, bg, 3.0)
                }
            }
        }
    }

    @Test
    fun visibleFillsNeedNoRing() {
        assertNull(CategoryPalette.ring(Color(0xFFFF2039), Color.White))
        assertTrue(contrast(CategoryPalette.ring(Color(0xFFFECBDC), Color.White)!!, Color.White) >= 3.0)
    }

    @Test
    fun displayIsTheSameInBothThemes() {
        val sky = CategoryPalette.colors[14]
        assertEquals(sky, CategoryPalette.display(sky, dark = true))
        assertEquals(sky, CategoryPalette.display(sky, dark = false))
    }

    @Test
    fun olderColoursShowAsTheNearestOfTheTwentyFour() {
        assertEquals(0xFF0EEF82, CategoryPalette.normalize(0xFF4CAF50)) // Material green → green
        assertEquals(0xFF0EEF82, CategoryPalette.normalize(0xFF4DFF88)) // 0.x neon green → green
        assertEquals(0xFF9E7756, CategoryPalette.normalize(0xFFD9A066)) // 0.x caramel → caramel
        assertEquals(0xFF95C11F, CategoryPalette.display(0xFFA8FF3E, dark = false)) // 0.x lime → lime
    }

    @Test
    fun foreignColoursAreKeptAsIs() {
        val imported = 0xFF123456
        assertEquals(imported, CategoryPalette.normalize(imported))
        assertEquals(imported, CategoryPalette.display(imported, dark = false))
    }

    @Test
    fun forKeyIsStableAndFromThePalette() {
        assertEquals(CategoryPalette.forKey("аня"), CategoryPalette.forKey("аня"))
        val picked = listOf("аня", "миша", "отпуск", "испанский", "кот", "работа").map(CategoryPalette::forKey)
        assert(picked.all { it in CategoryPalette.colors })
        assert(picked.toSet().size > 1) { "different tags should not all share one colour" }
    }

    private fun lab(c: Color): DoubleArray {
        fun lin(v: Float) = if (v <= 0.04045f) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        val r = lin(c.red)
        val g = lin(c.green)
        val b = lin(c.blue)
        val x = (0.4124 * r + 0.3576 * g + 0.1805 * b) / 0.95047
        val y = 0.2126 * r + 0.7152 * g + 0.0722 * b
        val z = (0.0193 * r + 0.1192 * g + 0.9505 * b) / 1.08883
        fun f(v: Double) = if (v > 0.008856) cbrt(v) else 7.787 * v + 16.0 / 116
        return doubleArrayOf(116 * f(y) - 16, 500 * (f(x) - f(y)), 200 * (f(y) - f(z)))
    }

    /** CIEDE2000, as in scripts/agent/colorlab.py. */
    private fun deltaE2000(p: Color, q: Color): Double {
        val (l1, a1r, b1) = lab(p).toList()
        val (l2, a2r, b2) = lab(q).toList()
        val cb = (hypot(a1r, b1) + hypot(a2r, b2)) / 2
        val g = 0.5 * (1 - sqrt(cb.pow(7) / (cb.pow(7) + 25.0.pow(7))))
        val a1 = (1 + g) * a1r
        val a2 = (1 + g) * a2r
        val c1 = hypot(a1, b1)
        val c2 = hypot(a2, b2)
        fun hue(a: Double, b: Double) = (Math.toDegrees(atan2(b, a)) + 360) % 360
        val h1 = hue(a1, b1)
        val h2 = hue(a2, b2)
        val dh = if (c1 * c2 == 0.0) 0.0 else (h2 - h1).let { if (it > 180) it - 360 else if (it < -180) it + 360 else it }
        val dH = 2 * sqrt(c1 * c2) * sin(Math.toRadians(dh / 2))
        val lb = (l1 + l2) / 2
        val cbp = (c1 + c2) / 2
        val hb = if (c1 * c2 == 0.0) h1 + h2 else if (kotlin.math.abs(h1 - h2) <= 180) (h1 + h2) / 2 else (h1 + h2 + 360) / 2
        val t = 1 - 0.17 * cos(Math.toRadians(hb - 30)) + 0.24 * cos(Math.toRadians(2 * hb)) +
            0.32 * cos(Math.toRadians(3 * hb + 6)) - 0.2 * cos(Math.toRadians(4 * hb - 63))
        val sl = 1 + 0.015 * (lb - 50).pow(2) / sqrt(20 + (lb - 50).pow(2))
        val sc = 1 + 0.045 * cbp
        val sh = 1 + 0.015 * cbp * t
        val rt = -2 * sqrt(cbp.pow(7) / (cbp.pow(7) + 25.0.pow(7))) * sin(Math.toRadians(60 * exp(-((hb - 275) / 25).pow(2))))
        val dl = l2 - l1
        val dc = c2 - c1
        return sqrt((dl / sl).pow(2) + (dc / sc).pow(2) + (dH / sh).pow(2) + rt * (dc / sc) * (dH / sh))
    }
}

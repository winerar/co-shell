package app.coshell.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import app.coshell.core.designsystem.category.CategoryPalette
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Release 1.0 check (#376): states, boundaries and secondary text in every palette × both themes.
 * WCAG: text ≥ 4.5, icons, state indicators and component boundaries ≥ 3 (1.4.11).
 *
 * Pairs that do not reach the target yet are [known] exceptions (#385, waiting for the owner's choice of a fix):
 * each has a floor — it may not get worse — and once every theme passes, the test asks to drop the exception.
 */
class NonTextContrastTest {
    private class Pair(val check: String, val theme: String, val fg: Color, val bg: Color, val target: Double)

    /** Current minimum (rounded down) of each pair below its target, #385. */
    private val known = mapOf(
        "tag label in its colour on its wash" to 3.5,
        "segment Choice pill on its track" to 1.0,
        "segment Filter pill on its track" to 1.0,
        "tab pill on the bar" to 1.0,
        "selected day on surface" to 1.15,
        "checkbox and radio outline on surface" to 1.8,
        "field fill on surface" to 1.05,
        "field, chip and card border on surface" to 1.25,
        "neon accent text inside the hero card" to 3.7,
    )

    private val pairs: List<Pair> = CoShellPalette.entries.flatMap { palette ->
        listOf(false, true).flatMap { dark -> pairsOf(palette, dark) }
    }

    @Test
    fun everyPairReachesItsTargetOrStaysAboveItsKnownFloor() {
        val problems = pairs.groupBy { it.check }.flatMap { (check, list) ->
            val floor = known[check] ?: list.first().target
            list.filter { contrast(it.fg, it.bg) < floor }
                .map { "$check · ${it.theme}: %.2f < %.2f".format(contrast(it.fg, it.bg), floor) }
        }
        assertTrue(problems.joinToString("\n", "Contrast fell below the target/floor:\n"), problems.isEmpty())
    }

    @Test
    fun fixedPairsLeaveTheKnownList() {
        val fixed = known.keys.filter { check ->
            val list = pairs.filter { it.check == check }
            assertTrue("known check «$check» has no pairs", list.isNotEmpty())
            list.all { contrast(it.fg, it.bg) >= it.target }
        }
        assertTrue("Reaches the target everywhere — remove from `known` (#385): $fixed", fixed.isEmpty())
    }

    private fun pairsOf(palette: CoShellPalette, dark: Boolean): List<Pair> {
        val t = themeColorsFor(palette, dark)
        val s = t.scheme
        val name = "$palette ${if (dark) "dark" else "light"}"
        val out = mutableListOf<Pair>()
        fun text(check: String, fg: Color, bg: Color) { out += Pair(check, name, fg, bg, 4.5) }
        fun shape(check: String, fg: Color, bg: Color) { out += Pair(check, name, fg, bg, 3.0) }

        // Secondary text: hints, unselected segments, search placeholder, field text, links.
        text("hint on surface", s.onSurfaceVariant, s.surface)
        text("hint on background", s.onSurfaceVariant, s.background)
        text("hint on a field or track", s.onSurfaceVariant, s.surfaceContainer)
        text("field text", s.onSurface, s.surfaceContainer)
        text("accent text on background", accentInkFor(s, dark), s.background)
        text("error on background", s.error, s.background)
        t.heroStops.forEach { text("neon accent text inside the hero card", accentInkFor(s, dark), it) }

        // Icons and marks.
        shape("tab icon on the bar", s.onSurfaceVariant, s.surfaceContainerHigh)
        shape("badge on the bar", s.tertiary, s.surfaceContainerHigh)
        shape("day dot on surface", s.tertiary, s.surface)
        shape("progress on surface", progressColorsFor(s, dark).indicator, s.surface)

        // Selected states and component boundaries.
        shape("segment Choice pill on its track", s.primary, s.surfaceContainer)
        shape("segment Filter pill on its track", s.secondaryContainer, s.surfaceContainer)
        shape("tab pill on the bar", if (dark) s.primary else s.primaryContainer, s.surfaceContainerHigh)
        shape("selected day on surface", s.primaryContainer, s.surface)
        shape("checkbox and radio outline on surface", s.outline, s.surface)
        shape("field fill on surface", s.surfaceContainer, s.surface)
        shape("field, chip and card border on surface", s.outlineVariant, s.surface)

        // Category and account colours (36 swatches) on every background they sit on.
        CategoryPalette.swatches.forEachIndexed { i, swatch ->
            val c = Color(if (dark) swatch.stored else swatch.light)
            listOf(s.surface, s.background, s.surfaceContainer).forEach { bg -> shape("category colour #$i on its background", c, bg) }
            text("text on a card in category colour #$i", s.onSurface, c.copy(alpha = CARD_TINT).compositeOver(s.surface))
            text("tag label in its colour on its wash", c, c.copy(alpha = TAG_FILL).compositeOver(s.background))
        }
        return out
    }

    private companion object {
        const val CARD_TINT = 0.12f // CoShellCard(tint)
        const val TAG_FILL = 0.18f // CoShellTagChip / CoShellTagPill
    }
}

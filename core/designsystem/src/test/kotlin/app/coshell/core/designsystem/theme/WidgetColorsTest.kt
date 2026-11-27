package app.coshell.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

/** The widget and the tile read in every theme (#795): text and button ink ≥ 4.5, the neon button and the card edge ≥ 3 against the card. */
class WidgetColorsTest {
    private val all = CoShellPalette.entries.flatMap { p -> listOf(false, true).map { dark -> "$p ${if (dark) "dark" else "light"}" to widgetColorsFor(p, dark) } }

    private fun ratio(a: Int, b: Int) = contrastRatio(Color(a), Color(b))

    @Test
    fun `text on the card and on the buttons is readable`() = all.forEach { (name, c) ->
        assertTrue("$name text on card", ratio(c.text, c.card) >= 4.5f)
        assertTrue("$name muted on card", ratio(c.muted, c.card) >= 4.5f)
        assertTrue("$name main ink", ratio(c.mainInk, c.main) >= 4.5f)
        assertTrue("$name quiet ink", ratio(c.quietInk, c.quiet) >= 4.5f)
    }

    @Test
    fun `the main button or its ring stands out from the card`() = all.forEach { (name, c) ->
        val best = maxOf(ratio(c.main, c.card), ratio(c.mainEdge, c.card))
        assertTrue("$name main vs card: $best", best >= 3f)
    }

    @Test
    fun `the card has a hairline distinct from its fill`() = all.forEach { (name, c) ->
        assertTrue("$name card edge", c.cardEdge != c.card)
    }
}

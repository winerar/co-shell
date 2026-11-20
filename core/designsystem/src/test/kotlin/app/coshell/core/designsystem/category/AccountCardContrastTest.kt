package app.coshell.core.designsystem.category

import androidx.compose.ui.graphics.Color
import app.coshell.core.designsystem.components.accountEdgeColor
import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.designsystem.theme.contrastRatio
import app.coshell.core.designsystem.theme.themeColorsFor
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Account cards and badges keep the account's own colour (owner 2026-11-20): any of the 24 palette colours or a bank
 * preset, in every theme × both modes. Their text ([inkOn]) reads at ≥ 4.5:1 on the fill, and the card stands out from the
 * page at ≥ 3:1 — by itself or by its [accountEdgeColor] hairline.
 *
 * [known] — brand colours no ink reaches 4.5:1 on (mid-luminance blue): kept as the brand has it, with a floor —
 * the owner's decision (2026-11-20: Revolut stays). A fixed one must leave the list.
 */
class AccountCardContrastTest {
    private val fills: List<Pair<String, Color>> =
        CategoryPalette.colors.mapIndexed { i, c -> "palette #$i" to Color(CategoryPalette.display(c, false)) } +
            BankColors.presets.map { it.name to Color(CategoryPalette.display(it.color, false)) }

    private val known = mapOf("Revolut" to 4.4f)

    @Test
    fun `text on an account colour reads`() {
        val bad = fills.filter { (name, c) -> contrastRatio(inkOn(c), c) < (known[name] ?: 4.5f) }
            .map { (name, c) -> "$name: %.2f".format(contrastRatio(inkOn(c), c)) }
        assertTrue("Account text under 4.5:1:\n" + bad.joinToString("\n"), bad.isEmpty())
    }

    @Test
    fun `known exceptions are still short`() {
        val fixed = known.keys.filter { name -> fills.filter { it.first == name }.all { (_, c) -> contrastRatio(inkOn(c), c) >= 4.5f } }
        assertTrue("Reads at 4.5:1 now — remove from `known`: $fixed", fixed.isEmpty())
    }

    @Test
    fun `account card stands out from the page in every theme`() {
        val bad = CoShellPalette.entries.flatMap { palette ->
            listOf(false, true).flatMap { dark ->
                val s = themeColorsFor(palette, dark).scheme
                listOf(s.background, s.surface).flatMap { page ->
                    fills.mapNotNull { (name, c) ->
                        val seen = accountEdgeColor(c, page) ?: c
                        val ratio = contrastRatio(seen, page)
                        if (ratio < 3f) "$name · $palette ${if (dark) "dark" else "light"}: %.2f".format(ratio) else null
                    }
                }
            }
        }
        assertTrue("Account card melts into the page:\n" + bad.joinToString("\n"), bad.isEmpty())
    }
}

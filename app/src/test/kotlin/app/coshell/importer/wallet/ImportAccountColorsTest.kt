package app.coshell.importer.wallet

import app.coshell.core.designsystem.category.BankColors
import app.coshell.core.designsystem.category.CategoryPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportAccountColorsTest {
    @Test
    fun knownBankGetsItsOwnColour() {
        val galicia = BankColors.presets.first { it.name == "Galicia" }
        assertEquals(galicia.color, ImportAccountColors.pick("Galicia ARS", index = 7))
    }

    @Test
    fun otherAccountsGetDistinctBrightColoursInOrder() {
        val colours = (0 until 15).map { ImportAccountColors.pick("Наличные $it", it) }
        assertEquals(15, colours.toSet().size)
        assertTrue(colours.all { it in CategoryPalette.colors.take(15) })
        assertTrue(colours.zipWithNext().none { (a, b) -> a == b })
    }
}

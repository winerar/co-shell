package app.coshell.budgets.list

import androidx.compose.ui.unit.Constraints
import app.coshell.budgets.list.components.LineSplit
import app.coshell.budgets.list.components.splitLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NameAndAmountTest {
    @Test
    fun shortAmountStaysNextToTheName() {
        // 300 px line, «Еда» 60 px, «310 / 400 ARS» 100 px: the name gets the rest.
        assertEquals(LineSplit(name = 190, amount = 100), splitLine(width = 300, nameNatural = 60, amountNatural = 100, gap = 10))
    }

    @Test
    fun longNameIsCutButKeepsItsShare() {
        // A long name still leaves room for the amount; it keeps ≥ 40 % (120 px) and gets «…».
        assertEquals(LineSplit(name = 150, amount = 140), splitLine(width = 300, nameNatural = 900, amountNatural = 140, gap = 10))
    }

    @Test
    fun longAmountGoesUnderTheName() {
        // «999 999 999,99 / 100 000 000,00 ARS» would leave the name less than 40 % — the amount takes its own line (#432).
        assertNull(splitLine(width = 300, nameNatural = 900, amountNatural = 200, gap = 10))
    }

    @Test
    fun shortNameLetsALongAmountStay() {
        // «Еда» needs only 50 px: the amount may take the rest of the line.
        assertEquals(LineSplit(name = 50, amount = 240), splitLine(width = 300, nameNatural = 50, amountNatural = 240, gap = 10))
    }

    @Test
    fun unboundedLineKeepsNaturalWidths() {
        assertEquals(LineSplit(80, 120), splitLine(width = Constraints.Infinity, nameNatural = 80, amountNatural = 120, gap = 10))
    }
}

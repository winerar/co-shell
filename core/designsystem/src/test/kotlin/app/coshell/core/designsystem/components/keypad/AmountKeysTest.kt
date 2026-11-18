package app.coshell.core.designsystem.components.keypad

import app.coshell.core.designsystem.components.keypad.KeypadKey.Backspace
import app.coshell.core.designsystem.components.keypad.KeypadKey.Decimal
import app.coshell.core.designsystem.components.keypad.KeypadKey.Digit
import app.coshell.core.designsystem.components.keypad.KeypadKey.Minus
import app.coshell.core.designsystem.components.keypad.KeypadKey.Plus
import app.coshell.core.designsystem.components.keypad.KeypadKey.TripleZero
import org.junit.Assert.assertEquals
import org.junit.Test

class AmountKeysTest {
    private fun type(vararg keys: KeypadKey, start: String = "", fraction: Int = 2) =
        keys.fold(start) { text, key -> AmountKeys.apply(text, key, fraction) }

    private fun digits(s: String) = s.map { Digit(it) }.toTypedArray()

    @Test
    fun typesDigitsWithoutLeadingZeros() {
        assertEquals("32000", type(*digits("32"), TripleZero))
        assertEquals("5", type(Digit('0'), Digit('5')))
        assertEquals("", type(TripleZero))
    }

    @Test
    fun decimalOnceAndWithinCurrencyFraction() {
        assertEquals("0,5", type(Decimal, Digit('5'), Decimal))
        assertEquals("1,25", type(Digit('1'), Decimal, Digit('2'), Digit('5'), Digit('9')))
        assertEquals("12", type(Digit('1'), Decimal, Digit('2'), fraction = 0))
    }

    @Test
    fun plusStartsANewTerm() {
        assertEquals("32 + 15", type(Digit('3'), Digit('2'), Plus, Plus, Digit('1'), Digit('5')))
        assertEquals("", type(Plus))
        assertEquals("1,", type(Digit('1'), Decimal, Plus))
    }

    @Test
    fun backspaceRemovesThePlusAsOneKey() {
        assertEquals("32", type(Digit('3'), Digit('2'), Plus, Backspace))
        assertEquals("3", type(Digit('3'), Digit('2'), Backspace))
        assertEquals("", type(Backspace))
    }

    @Test
    fun normalizesEditedAmounts() {
        assertEquals("1234,5", AmountKeys.normalize("1 234.5"))
        assertEquals("1234,56", type(Digit('6'), start = "1 234,5"))
    }

    @Test
    fun displayGroupsThousands() {
        assertEquals("0", AmountKeys.display(""))
        assertEquals("1 250 000 + 1,5", AmountKeys.display("1250000 + 1,5"))
    }

    @Test
    fun minusTakesAwayAndNeedsANumberBeforeIt() {
        assertEquals("1250 − 125", type(*digits("1250"), Minus, *digits("125")))
        assertEquals("", type(Minus))
        assertEquals("5,", type(Digit('5'), Decimal, Minus))
    }

    @Test
    fun anOperatorRightAfterAnotherReplacesIt() {
        assertEquals("10 − ", type(*digits("10"), Plus, Minus))
        assertEquals("10 + ", type(*digits("10"), Minus, Plus))
    }

    @Test
    fun backspaceRemovesAWholeMinus() {
        assertEquals("10", type(*digits("10"), Minus, Backspace))
        assertEquals("10 − 2", type(*digits("10"), Minus, Digit('2'), Digit('5'), Backspace))
    }

    @Test
    fun normalizeKeepsOperatorsAndDropsALeadingOne() {
        assertEquals("1234,5 − 20", AmountKeys.normalize("1 234.5 − 20"))
        assertEquals("12 + ", AmountKeys.normalize("12 +"))
        assertEquals("12", AmountKeys.normalize("− 12"))
    }

    @Test
    fun termsCarryTheirSigns() {
        assertEquals(
            listOf(AmountKeys.Term("1500", false), AmountKeys.Term("200", true), AmountKeys.Term("30,5", false)),
            AmountKeys.terms("1500 − 200 + 30,5"),
        )
        assertEquals(listOf(AmountKeys.Term("12", false)), AmountKeys.terms("12 + "))
    }

    @Test
    fun displayGroupsEveryTermAndKeepsTheOperator() {
        assertEquals("1\u2009250 − 1\u2009000,5", AmountKeys.display("1250 − 1000,5"))
    }

    @Test
    fun mapTermsKeepsOperators() {
        assertEquals("12 + 3 − 1", AmountKeys.mapTerms("12,5 + 3,25 − 1,5") { it.substringBefore(',') })
    }
}

package app.coshell.ui.form

import app.coshell.core.designsystem.components.keypad.KeypadKey
import org.junit.Test
import org.junit.Assert.assertEquals

class NumberKeysTest {
    private fun type(keys: String, maxWhole: Int, fraction: Int = 0): String = keys.fold("") { acc, c ->
        val key = when (c) {
            ',' -> KeypadKey.Decimal
            '<' -> KeypadKey.Backspace
            'Z' -> KeypadKey.TripleZero
            '+' -> KeypadKey.Plus
            else -> KeypadKey.Digit(c)
        }
        NumberKeys.apply(acc, key, maxWhole, fraction)
    }

    @Test
    fun `card digits keep leading zeros and stop at four`() = assertEquals("0123", type("012345", maxWhole = 4))

    @Test
    fun `percent takes two fraction digits and one comma`() = assertEquals("5,25", type("5,,257", maxWhole = 3, fraction = 2))

    @Test
    fun `comma first gives zero and comma`() = assertEquals("0,5", type(",5", maxWhole = 3, fraction = 2))

    @Test
    fun `no comma when there is no fraction`() = assertEquals("12", type("1,2", maxWhole = 3))

    @Test
    fun `triple zero fills only the room left`() = assertEquals("100", type("1Z", maxWhole = 3))

    @Test
    fun `backspace and plus`() = assertEquals("1", type("12<+", maxWhole = 3))

    @Test
    fun `normalize reads a saved dot and drops the rest`() {
        assertEquals("5,5", NumberKeys.normalize("5.5", maxWhole = 3, fractionDigits = 2))
        assertEquals("15", NumberKeys.normalize(" 15 ", maxWhole = 2))
    }
}

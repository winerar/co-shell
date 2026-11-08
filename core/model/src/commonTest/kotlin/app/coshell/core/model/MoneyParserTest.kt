package app.coshell.core.model

import app.coshell.core.model.MoneyParser.Reason
import app.coshell.core.model.MoneyParser.Result
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyParserTest {
    private val rub = CurrencyCode("RUB")
    private val jpy = CurrencyCode("JPY")
    private val kwd = CurrencyCode("KWD")

    private fun ok(input: String, currency: CurrencyCode = rub): Long =
        (MoneyParser.parse(input, currency) as Result.Success).money.amountMinor

    private fun err(input: String, currency: CurrencyCode = rub): Reason =
        (MoneyParser.parse(input, currency) as Result.Error).reason

    @Test
    fun `parses plain and decimal input`() {
        assertEquals(123400, ok("1234"))
        assertEquals(123450, ok("1234,5"))
        assertEquals(123456, ok("1234.56"))
        assertEquals(50, ok(",5"))
        assertEquals(500, ok("5,"))
        assertEquals(0, ok("0"))
    }

    @Test
    fun `ignores group separators`() {
        assertEquals(123456789, ok("1 234 567,89"))
        assertEquals(123456789, ok("1 234 567,89"))
        assertEquals(123456789, ok("1,234,567.89"))
        assertEquals(123456789, ok("1.234.567,89"))
        assertEquals(123456700, ok("1.234.567"))
        assertEquals(123400, ok("1.234"))
        assertEquals(123456, ok("1'234.56"))
    }

    @Test
    fun `handles sign`() {
        assertEquals(-1230, ok("-12,3"))
        assertEquals(-1230, ok("−12,30"))
        assertEquals(1230, ok("+12,30"))
    }

    @Test
    fun `follows currency fraction digits`() {
        assertEquals(1500, ok("1500", jpy))
        assertEquals(1500, ok("1.500", jpy))
        assertEquals(1234, ok("1.234", kwd))
        assertEquals(Reason.TOO_MANY_FRACTION_DIGITS, err("12.3456", kwd))
        assertEquals(Reason.TOO_MANY_FRACTION_DIGITS, err("1,2345"))
        assertEquals(Reason.TOO_MANY_FRACTION_DIGITS, err("1,234.567"))
    }

    @Test
    fun `reports errors`() {
        assertEquals(Reason.EMPTY, err(""))
        assertEquals(Reason.EMPTY, err("  "))
        assertEquals(Reason.EMPTY, err("-"))
        assertEquals(Reason.INVALID_CHARACTERS, err("12a"))
        assertEquals(Reason.INVALID_CHARACTERS, err("1-2"))
        assertEquals(Reason.TOO_LARGE, err("999999999999999999999"))
    }
}

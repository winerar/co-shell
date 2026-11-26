package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AmountSignTest {
    @Test
    fun `adds minus to a positive amount`() {
        assertEquals("-12,5", AmountSign.toggle("12,5"))
        assertEquals("-", AmountSign.toggle(""))
        assertEquals("-7", AmountSign.toggle("+7"))
        assertEquals("-3", AmountSign.toggle("  3"))
    }

    @Test
    fun `removes any minus`() {
        assertEquals("12,5", AmountSign.toggle("-12,5"))
        assertEquals("12,5", AmountSign.toggle("−12,5"))
    }

    @Test
    fun `toggled text parses as a negative amount`() {
        val usd = CurrencyCode("USD")
        assertEquals(-150_00L, MoneyParser.parseOrNull(AmountSign.toggle("150"), usd)?.amountMinor)
    }

    @Test
    fun `the sign is read and put back without touching the digits`() {
        assertEquals(true, AmountSign.isNegative("-12,5"))
        assertEquals(true, AmountSign.isNegative(" \u221212"))
        assertEquals(false, AmountSign.isNegative("12,5"))
        assertEquals(false, AmountSign.isNegative(""))
        assertEquals("12,5", AmountSign.magnitude("-12,5"))
        assertEquals("3", AmountSign.magnitude("+3"))
        assertEquals("-12,5", AmountSign.signed("12,5", negative = true))
        assertEquals("12,5", AmountSign.signed("-12,5", negative = false))
        assertEquals("", AmountSign.signed("", negative = true))
    }
}

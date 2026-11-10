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
}

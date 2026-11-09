package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CurrenciesTest {

    @Test
    fun `knows fraction digits of ISO currencies`() {
        assertEquals(2, Currencies.info("RUB").fractionDigits)
        assertEquals(0, Currencies.info("JPY").fractionDigits)
        assertEquals(3, Currencies.info("KWD").fractionDigits)
        assertEquals(2, Currencies.info("XYZ").fractionDigits) // unknown -> default
    }

    @Test
    fun `knows custom assets`() {
        assertEquals(8, Currencies.info("BTC").fractionDigits)
        assertEquals(100_000_000L, Currencies.info("BTC").minorPerMajor)
    }

    @Test
    fun `validates currency codes`() {
        assertFailsWith<IllegalArgumentException> { CurrencyCode("rub") }
        assertFailsWith<IllegalArgumentException> { CurrencyCode("RU") }
        assertEquals(CurrencyCode("RUB"), CurrencyCode.parseOrNull(" rub "))
        assertNull(CurrencyCode.parseOrNull("руб"))
    }
}

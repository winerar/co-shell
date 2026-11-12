package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class RateBookCacheTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val nov9 = LocalDate(2026, 11, 9)

    private fun rates(rate: String) = listOf(ExchangeRate(nov9, usd, ars, Rate.parse(rate), RateOrigin.DOWNLOADED))

    @Test
    fun `the same list gives the same book`() {
        val list = rates("1450")
        assertSame(RateBook.of(list), RateBook.of(list))
    }

    @Test
    fun `a new list of rates builds a new book with its rates`() {
        val old = RateBook.of(rates("1450"))
        val fresh = RateBook.of(rates("1500"))

        assertNotSame(old, fresh)
        assertEquals(Rate.parse("1500"), fresh.find(usd, ars, nov9)?.rate)
    }
}

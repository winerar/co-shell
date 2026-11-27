package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RateTrendTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val today = LocalDate(2026, 11, 30)

    private fun rate(day: LocalDate, value: String) = ExchangeRate(day, usd, ars, Rate.parse(value), RateOrigin.DOWNLOADED)

    @Test
    fun `a rising rate gives sixteen points and a positive change in basis points`() {
        val book = RateBook.of(listOf(rate(LocalDate(2026, 10, 25), "1000"), rate(LocalDate(2026, 11, 30), "1100")))
        val trend = assertNotNull(RateTrends.of(book, usd, ars, today))
        assertEquals(16, trend.values.size)
        assertEquals(1000, trend.changeBp)
        assertTrue(trend.values.first() < trend.values.last())
    }

    @Test
    fun `the other direction is the inverse rate`() {
        val book = RateBook.of(listOf(rate(LocalDate(2026, 10, 25), "1000"), rate(LocalDate(2026, 11, 30), "1250")))
        val trend = assertNotNull(RateTrends.of(book, ars, usd, today))
        assertEquals(-2000, trend.changeBp)
    }

    @Test
    fun `no history from before the start gives no trend`() {
        val book = RateBook.of(listOf(rate(LocalDate(2026, 11, 20), "1000")))
        assertNull(RateTrends.of(book, usd, ars, today))
        assertNull(RateTrends.of(RateBook.of(emptyList()), usd, ars, today))
    }

    @Test
    fun `the base against itself has no trend`() {
        assertNull(RateTrends.of(RateBook.of(listOf(rate(LocalDate(2026, 10, 1), "1000"))), usd, usd, today))
    }
}

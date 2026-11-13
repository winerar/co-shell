package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AmountInCurrenciesTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val ves = CurrencyCode("VES")

    private fun day(d: Int) = LocalDate(2026, 11, d)
    private fun rate(d: Int, from: CurrencyCode, to: CurrencyCode, value: String) =
        ExchangeRate(day(d), from, to, Rate.parse(value), RateOrigin.DOWNLOADED)

    private val converter = CurrencyConverter(
        RateBook(listOf(rate(1, usd, ars, "1000"), rate(10, usd, ars, "1100"), rate(1, usd, amd, "400"))),
    )

    @Test
    fun `converts into every currency in order and skips the amount's own`() {
        val lines = converter.amountIn(Money.ofMajor(11_000, currency = ars), listOf(usd, ars, amd), day(12))
        assertEquals(listOf(usd, amd), lines.map { it.currency })
        assertEquals(Money.ofMajor(10, currency = usd), lines[0].money)
        assertEquals(Money.ofMajor(4_000, currency = amd), lines[1].money)
    }

    @Test
    fun `uses the rate of the given date`() {
        val early = converter.amountIn(Money.ofMajor(10_000, currency = ars), listOf(usd), day(2)).single()
        assertEquals(Money.ofMajor(10, currency = usd), early.money)
    }

    @Test
    fun `currency without a rate stays in the list with no amount`() {
        val lines = converter.amountIn(Money.ofMajor(5, currency = usd), listOf(ves, ars), day(12))
        assertEquals(listOf(ves, ars), lines.map { it.currency })
        assertNull(lines[0].money)
        assertEquals(Money.ofMajor(5_500, currency = ars), lines[1].money)
    }

    @Test
    fun `duplicates are shown once`() {
        val lines = converter.amountIn(Money.ofMajor(1, currency = usd), listOf(ars, ars, amd), day(12))
        assertEquals(listOf(ars, amd), lines.map { it.currency })
    }
}

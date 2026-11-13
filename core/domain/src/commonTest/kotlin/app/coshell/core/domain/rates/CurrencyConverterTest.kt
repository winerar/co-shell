package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CurrencyConverterTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val rub = CurrencyCode("RUB")
    private val eur = CurrencyCode("EUR")

    private fun day(d: Int) = LocalDate(2026, 11, d)
    private fun rate(d: Int, from: CurrencyCode, to: CurrencyCode, value: String, manual: Boolean = false) =
        ExchangeRate(day(d), from, to, Rate.parse(value), if (manual) RateOrigin.MANUAL else RateOrigin.DOWNLOADED)

    private val downloads = listOf(
        rate(1, usd, ars, "1000"),
        rate(5, usd, ars, "1100"),
        rate(1, usd, amd, "400"),
        rate(5, usd, amd, "390"),
        rate(5, usd, rub, "80"),
    )

    private fun converter(vararg extra: ExchangeRate) = CurrencyConverter(RateBook(downloads + extra))

    @Test
    fun `same currency needs no rate`() {
        val result = converter().convert(Money(123, eur), eur, day(1))!!
        assertEquals(Money(123, eur), result.money)
    }

    @Test
    fun `uses the rate on the date, else the nearest earlier one`() {
        val c = converter()
        assertEquals(Money(100_000, ars), c.convert(Money(100, usd), ars, day(1))!!.money)
        assertEquals(Money(100_000, ars), c.convert(Money(100, usd), ars, day(4))!!.money)
        val onFifth = c.convert(Money(100, usd), ars, day(9))!!
        assertEquals(Money(110_000, ars), onFifth.money)
        assertEquals(day(5), onFifth.rate.date)
    }

    @Test
    fun `serves the opposite direction`() {
        // 1 100.00 ARS on Nov 5 = 1.00 USD
        assertEquals(Money(100, usd), converter().convert(Money(110_000, ars), usd, day(5))!!.money)
    }

    @Test
    fun `crosses through USD`() {
        // 1 000 ARS → 1 USD → 400 AMD on Nov 1
        val result = converter().convert(Money(100_000, ars), amd, day(1))!!
        assertEquals(Money(40_000, amd), result.money)
        assertTrue(result.rate.viaPivot)
        assertEquals(RateOrigin.DOWNLOADED, result.rate.origin)
    }

    @Test
    fun `manual rate wins and stays until the next manual rate`() {
        val blue = rate(2, usd, ars, "1400", manual = true)
        val c = converter(blue)
        assertEquals(Money(100_000, ars), c.convert(Money(100, usd), ars, day(1))!!.money)
        val later = c.convert(Money(100, usd), ars, day(9))!! // the Nov 5 download does not override it
        assertEquals(Money(140_000, ars), later.money)
        assertEquals(RateOrigin.MANUAL, later.rate.origin)
        // Manual leg propagates into cross rates: 1 400 ARS = 1 USD = 390 AMD on Nov 9.
        val cross = c.convert(Money(140_000, ars), amd, day(9))!!
        assertEquals(Money(39_000, amd), cross.money)
        assertEquals(RateOrigin.MANUAL, cross.rate.origin)
    }

    @Test
    fun `falls back to the nearest later rate only when nothing earlier exists`() {
        val result = converter().convert(Money(100, usd), rub, day(1))!!
        assertEquals(Money(8_000, rub), result.money)
        assertEquals(day(5), result.rate.date)
    }

    @Test
    fun `cross leg with only an earlier rate keeps it while the other leg takes a later one`() {
        // EUR is known only from a hand-set rate on Nov 1, RUB only from Nov 5: on Nov 3 the cross still works.
        val result = converter(rate(1, eur, usd, "1.25", manual = true)).convert(Money(100, eur), rub, day(3))!!
        assertEquals(Money(10_000, rub), result.money)
        assertTrue(result.rate.viaPivot)
        assertEquals(day(1), result.rate.date)
    }

    @Test
    fun `unknown pair gives null and totals list what is missing`() {
        val c = converter()
        assertNull(c.convert(Money(100, eur), ars, day(5)))
        val total = c.total(
            listOf(Money(100, usd) to day(5), Money(55_000, ars) to day(5), Money(100, eur) to day(5)),
            ars,
        )
        assertEquals(Money(165_000, ars), total.sum)
        assertEquals(setOf(eur), total.missing)
        assertFalse(c.total(emptyList(), ars).missing.isNotEmpty())
    }
}

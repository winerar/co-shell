package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CurrentRatesTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val eur = CurrencyCode("EUR")

    private fun day(d: Int) = LocalDate(2026, 11, d)
    private fun rate(d: Int, from: CurrencyCode, to: CurrencyCode, value: String, origin: RateOrigin = RateOrigin.DOWNLOADED) =
        ExchangeRate(day(d), from, to, Rate.parse(value), origin)

    private val book = RateBook(
        listOf(
            rate(1, usd, ars, "1000"),
            rate(5, usd, ars, "1100"),
            rate(5, usd, amd, "390"),
            rate(3, ars, usd, "0.001", RateOrigin.MANUAL),
        ),
    )

    @Test
    fun `lists every currency but the base, sorted, with the rate in force`() {
        val rows = book.currentRates(setOf(usd, ars, amd, eur), base = usd, date = day(9))

        assertEquals(listOf(amd, ars, eur), rows.map { it.currency })
        assertEquals(Rate.parse("390"), rows[0].found!!.rate)
        assertEquals(day(5), rows[0].found!!.date)
        assertNull(rows[2].found)
    }

    @Test
    fun `a manual rate wins over a later download`() {
        val ars = book.currentRates(setOf(ars), base = usd, date = day(9)).single().found!!

        assertEquals(0, Rate.parse("1000").compareTo(ars.rate))
        assertEquals(RateOrigin.MANUAL, ars.origin)
        assertEquals(day(3), ars.date)
    }

    @Test
    fun `a non-pivot base uses cross rates`() {
        val amd = book.currentRates(setOf(usd, amd), base = ars, date = day(9)).first { it.currency == amd }.found!!
        assertEquals(true, amd.viaPivot)
    }
}

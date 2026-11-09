package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class KeepOnlyTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val eur = CurrencyCode("EUR")
    private val usdt = CurrencyCode("USDT")
    private val day = LocalDate(2026, 11, 9)

    private fun rate(from: CurrencyCode, to: CurrencyCode, value: String) =
        ExchangeRate(day, from, to, Rate.parse(value), RateOrigin.DOWNLOADED)

    @Test
    fun `keeps only used currencies`() {
        val snapshot = RatesSnapshot(day, usd, listOf(rate(usd, ars, "1440"), rate(usd, eur, "0.92")), "test")
        assertEquals(listOf(rate(usd, ars, "1440")), snapshot.keepOnly(setOf(ars, usd)).rates)
        assertEquals(day, snapshot.keepOnly(setOf(ars)).date)
    }

    @Test
    fun `keeps the pivot even if no account uses it`() {
        val snapshot = RatesSnapshot(day, eur, listOf(rate(eur, usd, "1.08"), rate(eur, ars, "1560"), rate(eur, usdt, "1.08")), "test")
        assertEquals(listOf(rate(eur, usd, "1.08"), rate(eur, usdt, "1.08")), snapshot.keepOnly(setOf(usdt)).rates)
    }
}

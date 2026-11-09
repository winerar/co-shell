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

class BalanceTotalsTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val rub = CurrencyCode("RUB")

    private fun day(d: Int) = LocalDate(2026, 11, d)
    private fun money(major: Long, currency: CurrencyCode) = Money.ofMajor(major, currency = currency)

    private val book = RateBook(
        listOf(
            ExchangeRate(day(5), usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED),
            ExchangeRate(day(8), usd, amd, Rate.parse("400"), RateOrigin.DOWNLOADED),
        ),
    )

    @Test
    fun `sums balances in every report currency`() {
        val balances = listOf(money(100, usd), money(50_000, ars), money(20_000, amd))

        val (inUsd, inArs) = book.balanceTotals(balances, listOf(usd, ars), day(9))

        assertEquals(money(200, usd), inUsd.sum) // 100 + 50 + 50
        assertEquals(money(200_000, ars), inArs.sum) // 100 000 + 50 000 + 50 000 (cross via USD)
        assertEquals(day(5), inUsd.rateDate) // the older of the two rates used
    }

    @Test
    fun `currencies without a rate are left out and listed`() {
        val total = book.balanceTotals(listOf(money(10, usd), money(500, rub)), listOf(usd), day(9)).single()

        assertEquals(money(10, usd), total.sum)
        assertEquals(setOf(rub), total.missing)
    }

    @Test
    fun `nothing to convert has no rate date`() {
        val total = book.balanceTotals(listOf(money(10, usd), money(-3, usd)), listOf(usd), day(9)).single()

        assertEquals(money(7, usd), total.sum)
        assertNull(total.rateDate)
    }

    @Test
    fun `balances of one currency are converted once`() {
        // 5 ARS = 0.005 USD rounds (half-even) to 0 cents each; 15 ARS = 0.015 USD rounds to 2 cents.
        val balances = List(3) { Money(500, ars) }
        val total = book.balanceTotals(balances, listOf(usd), day(9)).single()

        assertEquals(Money(2, usd), total.sum)
    }
}

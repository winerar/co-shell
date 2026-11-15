package app.coshell.transactions.list

import app.coshell.core.designsystem.components.PeekLine
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountPeekTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val nbsp = MoneyFormat.NARROW_NBSP
    private fun day(d: Int) = LocalDate(2026, 11, d)
    private fun rate(d: Int, to: CurrencyCode, value: String) = ExchangeRate(day(d), usd, to, Rate.parse(value), RateOrigin.DOWNLOADED)

    private val book = RateBook(listOf(rate(1, ars, "1000"), rate(10, ars, "1100")))

    private fun peek(money: Money, currencies: List<CurrencyCode>, date: LocalDate = day(12)) =
        book.amountPeek("t1", money, date, currencies, MoneyFormat.RU, "нет курса")

    @Test
    fun `lines in the report currencies without the amount's own, at the operation day`() {
        val result = peek(Money.ofMajor(11_000, currency = ars), listOf(usd, ars, amd))!!
        assertEquals("t1", result.rowId)
        assertEquals(day(12), result.date)
        assertEquals(listOf(PeekLine("10,00", "USD", "нет курса"), PeekLine(null, "AMD", "нет курса")), result.lines)
    }

    @Test
    fun `an older day uses its own rate`() {
        val result = peek(Money.ofMajor(11_000, currency = ars), listOf(usd), date = day(5))!!
        assertEquals("11,00", result.lines.single().amount)
    }

    @Test
    fun `thousands are grouped`() {
        val result = peek(Money.ofMajor(1_500, currency = usd), listOf(ars))!!
        assertEquals("1${nbsp}650${nbsp}000,00", result.lines.single().amount)
    }

    @Test
    fun `nothing to show when the only report currency is the amount's own`() {
        assertNull(peek(Money.ofMajor(10, currency = usd), listOf(usd)))
    }
}

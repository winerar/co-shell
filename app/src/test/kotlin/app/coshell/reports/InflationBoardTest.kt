package app.coshell.reports

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Траты в ARS и в USD» (#788): which currencies are compared and over which months. */
class InflationBoardTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val categories = listOf(Category("food", "Продукты", CategoryType.EXPENSE))
    private val rates = listOf(1000L, 1100L, 1210L, 1331L, 1464L, 1610L)
    private val book = RateBook(rates.mapIndexed { i, r -> ExchangeRate(LocalDate(2026, 6 + i, 1), usd, ars, Rate.parse(r.toString()), RateOrigin.DOWNLOADED) })
    private val days = rates.mapIndexed { i, r -> ReportDay(LocalDate(2026, 6 + i, 5), "food", TransactionType.EXPENSE, Money(100 * r * 100, ars)) } +
        ReportDay(LocalDate(2026, 12, 3), "food", TransactionType.EXPENSE, Money(100_000, ars))

    private fun inflation(reportCurrency: CurrencyCode, currencies: List<CurrencyCode>) = buildInflation(
        ReportInput(ReportLedger(days, categories, book, reportCurrency), categories, ReportPeriod.month(YearMonth(2026, 12)), 1, LocalDate(2026, 12, 10), compare = true),
        days,
        book,
        currencies,
    )

    @Test
    fun `pesos against the report currency over the finished months, the running one left out`() {
        val card = inflation(usd, listOf(usd, ars))!!
        assertEquals("ARS", card.local)
        assertEquals("USD", card.hard)
        assertEquals(YearMonth(2026, 11), card.months.last())
        assertEquals(0, card.hardPercent)
        assertTrue(card.pricesNotSpending)
    }

    @Test
    fun `a report in pesos compares with the next totals currency, one currency only gives nothing`() {
        assertEquals("USD", inflation(ars, listOf(ars, usd))!!.hard)
        assertNull(inflation(ars, listOf(ars)))
    }
}

package app.coshell.reports

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Необычное» (#787): which folders the card lists and how the bar and the usual mark sit on one scale. */
class UnusualBoardTest {
    private val usd = CurrencyCode("USD")
    private val categories = listOf(Category("cafe", "Кафе", CategoryType.EXPENSE), Category("home", "Дом", CategoryType.EXPENSE))
    private val format = MoneyFormat.RU.copy(trimZeroFraction = true)

    private fun spent(month: Int, id: String, units: Long) = ReportDay(LocalDate(2026, month, 5), id, TransactionType.EXPENSE, Money(units * 100, usd))

    private fun unusual(period: ReportPeriod, today: LocalDate, vararg days: ReportDay) = buildUnusual(
        ReportInput(ReportLedger(days.toList(), categories, RateBook.of(emptyList()), usd), categories, period, 1, today, compare = true),
        format,
    )

    private val history = (8..10).flatMap { listOf(spent(it, "cafe", 100), spent(it, "home", 1_000)) }.toTypedArray()

    @Test
    fun `a folder spending twice its usual is listed with the usual as the grey mark`() {
        val lines = unusual(ReportPeriod.month(YearMonth(2026, 11)), LocalDate(2026, 11, 30), *history, spent(11, "cafe", 200), spent(11, "home", 1_000))
        assertEquals(listOf("cafe"), lines.map { it.categoryId })
        val cafe = lines.single()
        assertEquals("200", cafe.value)
        assertEquals(1f, cafe.fraction, 0.001f)
        assertEquals(0.5f, cafe.usual, 0.001f)
        assertEquals(100, cafe.mark.percent)
        assertTrue(cafe.mark.up)
    }

    @Test
    fun `only a month report has the card`() {
        val quarter = ReportPeriod.quarter(YearMonth(2026, 10), 1)
        assertTrue(unusual(quarter, LocalDate(2026, 11, 30), *history, spent(11, "cafe", 900)).isEmpty())
    }
}

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomeBoardTest {
    private val usd = CurrencyCode("USD")
    private val categories = listOf(Category("cafe", "Кафе", CategoryType.EXPENSE), Category("salary", "Зарплата", CategoryType.INCOME))
    private val today = LocalDate(2026, 11, 11)

    private fun day(month: Int, category: String, units: Long, type: TransactionType = TransactionType.EXPENSE) =
        ReportDay(LocalDate(2026, month, 5), category, type, Money(units * 100, usd))

    private fun income(period: ReportPeriod, vararg days: ReportDay) = buildIncome(
        ReportInput(ReportLedger(days.toList(), categories, RateBook.of(emptyList()), usd), categories, period, 1, today, compare = true),
        MoneyFormat.RU.copy(trimZeroFraction = true),
    )

    @Test
    fun `six months on one scale, the shown month selected with what was left`() {
        val income = income(
            ReportPeriod.month(YearMonth(2026, 10)),
            day(9, "salary", 1_000, TransactionType.INCOME), day(9, "cafe", 1_100),
            day(10, "salary", 2_000, TransactionType.INCOME), day(10, "cafe", 1_500),
        )!!

        assertEquals((5..10).map { YearMonth(2026, it) }, income.columns.map { it.month })
        assertEquals(1f, income.columns.last().earnedFraction, 0.001f)
        assertEquals(0.55f, income.columns[4].spentFraction, 0.001f)
        assertEquals(5, income.selected)
        assertEquals("+500", income.saved)
        assertFalse(income.overspent)
        assertEquals(25, income.rate)
        assertFalse(income.running)
        assertEquals(13, income.averageRate)
        assertEquals(2, income.averageMonths)
    }

    @Test
    fun `a minus month and a running one`() {
        val september = income(ReportPeriod.month(YearMonth(2026, 9)), day(9, "salary", 1_000, TransactionType.INCOME), day(9, "cafe", 1_100))!!
        assertEquals("-100", september.saved.replace('−', '-'))
        assertTrue(september.overspent)
        assertEquals(-10, september.rate)

        val november = income(ReportPeriod.month(YearMonth(2026, 11)), day(11, "cafe", 50))!!
        assertTrue(november.running)
        assertNull(november.rate)
        assertNull(november.averageRate)
    }

    @Test
    fun `nothing in the months — no card`() {
        assertNull(income(ReportPeriod.month(YearMonth(2026, 11))))
    }
}

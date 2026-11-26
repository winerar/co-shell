package app.coshell.reports

import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.NetWorth
import app.coshell.core.domain.reports.NetWorthPoint
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import app.coshell.reports.ReportsUiState.CushionGloss
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** «Подушка» (#241): the card's numbers and which phrase it says. */
class CushionBoardTest {
    private val usd = CurrencyCode("USD")
    private val categories = listOf(Category("cafe", "Кафе", CategoryType.EXPENSE))
    private val today = LocalDate(2026, 11, 11)
    private val format = MoneyFormat.RU.copy(trimZeroFraction = true)
    private val november = ReportPeriod.month(YearMonth(2026, 11))

    private fun spent(month: Int, units: Long) = ReportDay(LocalDate(2026, month, 5), "cafe", TransactionType.EXPENSE, Money(units * 100, usd))

    private fun worth(units: Long, missing: Set<CurrencyCode> = emptySet()): NetWorth {
        val running = budgetPeriod(YearMonth(2026, 11), 1)
        return NetWorth(listOf(NetWorthPoint(running, today, Money(units * 100, usd), running = true)), emptyList(), missing)
    }

    private fun cushion(worth: NetWorth, vararg days: ReportDay) = buildCushion(
        ReportInput(ReportLedger(days.toList(), categories, RateBook.of(emptyList()), usd), categories, november, 1, today, compare = true),
        worth,
        format,
    )

    private val halfYear = (5..10).map { spent(it, 2_000) }.toTypedArray()

    @Test
    fun `half a year of spending gives the months, the average and the scale`() {
        val c = cushion(worth(9_000), *halfYear, spent(11, 300))

        assertEquals("4,5", c.months)
        assertEquals(0.75f, c.fraction, 0.001f)
        assertEquals(CushionGloss.NORMAL, c.gloss)
        assertEquals("2 000 USD", c.monthlySpend)
        assertEquals("9 000 USD", c.money)
    }

    @Test
    fun `the phrase goes empty, low, few months, no rate in this order`() {
        assertEquals(CushionGloss.EMPTY, cushion(worth(0), *halfYear).gloss)
        val low = cushion(worth(1_600), *halfYear)
        assertEquals(CushionGloss.LOW, low.gloss)
        assertEquals(3, low.weeks)
        val few = cushion(worth(9_000, missing = setOf(CurrencyCode("AMD"))), spent(9, 2_000), spent(10, 2_000))
        assertEquals(CushionGloss.FEW, few.gloss)
        assertEquals(2, few.historyMonths)
        val noRate = cushion(worth(9_000, missing = setOf(CurrencyCode("AMD"))), *halfYear)
        assertEquals(CushionGloss.NO_RATE, noRate.gloss)
        assertEquals(listOf("AMD"), noRate.missing)
    }

    @Test
    fun `from ten months on the number is whole and the scale is full`() {
        val c = cushion(worth(25_000), *halfYear)
        assertEquals("12", c.months)
        assertEquals(1f, c.fraction, 0.001f)
    }

    @Test
    fun `before the first full month there is only the phrase`() {
        val c = cushion(worth(9_000), spent(11, 500))
        assertNull(c.months)
        assertEquals(CushionGloss.NOT_YET, c.gloss)
    }

    @Test
    fun `the report loads half a year back for the cushion`() {
        val (from, _) = november.loadRange(1, today)
        assertEquals(LocalDate(2026, 5, 1), from)
    }
}

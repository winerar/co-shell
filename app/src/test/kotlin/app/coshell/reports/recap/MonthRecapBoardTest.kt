package app.coshell.reports.recap

import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.reports.RecapCard
import app.coshell.core.domain.reports.RecapPhrase
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Итоги октября» shown on 2 November 2026: September is the month before, October is told. */
class MonthRecapBoardTest {
    private val usd = CurrencyCode("USD")
    private val october = YearMonth(2026, 10)
    private val today = LocalDate(2026, 11, 2)
    private val format = MoneyFormat.RU.copy(trimZeroFraction = true)
    private val categories = listOf(
        Category("food", "Продукты", CategoryType.EXPENSE),
        Category("cafe", "Кафе", CategoryType.EXPENSE),
        Category("salary", "Зарплата", CategoryType.INCOME),
    )

    private fun spent(month: Int, day: Int, id: String, major: Long) =
        ReportDay(LocalDate(2026, month, day), id, TransactionType.EXPENSE, Money.ofMajor(major, currency = usd))

    private fun snapshot(days: List<ReportDay>) = BudgetSource.Snapshot(emptyList(), categories, days, emptyList(), usd, startDay = 1)

    private fun recap(vararg days: ReportDay) = buildMonthRecap(snapshot(days.toList()), october, today, format)

    @Test
    fun `a month with nothing in it has no stories`() {
        val state = recap(spent(9, 5, "food", 100))
        assertTrue(state.cards.isEmpty())
        assertEquals(october, state.month)
    }

    @Test
    fun `top categories, weeks and the comparison with the month before`() {
        val state = recap(
            spent(9, 5, "food", 400),
            spent(10, 2, "food", 100), spent(10, 3, "cafe", 100), spent(10, 9, "food", 100),
        )
        assertEquals(listOf("food", "cafe"), state.top.map { it.category.id })
        assertEquals(listOf(67, 33), state.top.map { it.percent })
        assertEquals(-25, state.changePercent)
        assertEquals(5, state.weeks.size)
        assertEquals("1–7", state.weeks.first().label)
        assertEquals("29–31", state.weeks.last().label)
        assertEquals(0, state.busiestWeek)
        assertEquals(RecapPhrase.GOOD_START, state.phrase)
        assertEquals(RecapCard.FINAL, state.cards.last())
    }

    @Test
    fun `without a month before there is nothing to compare with`() {
        val state = recap(spent(10, 2, "food", 100))
        assertNull(state.changePercent)
        assertNull(state.previousSpent)
        assertEquals(listOf(RecapCard.TOP_CATEGORIES, RecapCard.WEEKS, RecapCard.WEEKEND, RecapCard.FINAL).minus(RecapCard.WEEKEND), state.cards.filter { it != RecapCard.WEEKEND })
    }
}

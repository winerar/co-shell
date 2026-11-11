package app.coshell.core.domain.budgets

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BudgetPeriodTest {
    private val november = YearMonth(2026, 11)

    @Test
    fun `by default the period is the calendar month`() {
        val period = budgetPeriod(november)

        assertEquals(LocalDate(2026, 11, 1), period.start)
        assertEquals(LocalDate(2026, 12, 1), period.endExclusive)
        assertEquals(LocalDate(2026, 11, 30), period.lastDay)
        assertEquals(30, period.days)
        assertTrue(LocalDate(2026, 11, 30) in period)
        assertFalse(LocalDate(2026, 12, 1) in period)
    }

    @Test
    fun `a period from payday is named after the month it starts in`() {
        val period = budgetPeriod(YearMonth(2026, 12), startDay = 10)

        assertEquals(LocalDate(2026, 12, 10), period.start)
        assertEquals(LocalDate(2027, 1, 10), period.endExclusive)
        assertEquals(31, period.days)
    }

    @Test
    fun `a start day the month does not have falls on its last day`() {
        val february = budgetPeriod(YearMonth(2027, 2), startDay = 31)

        assertEquals(LocalDate(2027, 2, 28), february.start)
        assertEquals(LocalDate(2027, 3, 31), february.endExclusive)
        assertFailsWith<IllegalArgumentException> { budgetPeriod(november, startDay = 0) }
        assertFailsWith<IllegalArgumentException> { budgetPeriod(november, startDay = 32) }
    }

    @Test
    fun `the period of a day depends on the start day`() {
        assertEquals(november, budgetPeriodOf(LocalDate(2026, 11, 11)).month)
        assertEquals(november, budgetPeriodOf(LocalDate(2026, 11, 10), startDay = 10).month)
        assertEquals(YearMonth(2026, 10), budgetPeriodOf(LocalDate(2026, 11, 9), startDay = 10).month)
        assertEquals(YearMonth(2026, 12), budgetPeriodOf(LocalDate(2027, 1, 3), startDay = 10).month)
    }

    @Test
    fun `days passed include today and stop at the edges`() {
        val period = budgetPeriod(november)

        assertEquals(11, period.daysPassed(LocalDate(2026, 11, 11)))
        assertEquals(19, period.daysLeft(LocalDate(2026, 11, 11)))
        assertEquals(0, period.daysLeft(LocalDate(2026, 11, 30)))
        assertEquals(0, period.daysPassed(LocalDate(2026, 10, 20)))
        assertEquals(30, period.daysLeft(LocalDate(2026, 10, 20)))
        assertEquals(30, period.daysPassed(LocalDate(2027, 1, 5)))
    }
}

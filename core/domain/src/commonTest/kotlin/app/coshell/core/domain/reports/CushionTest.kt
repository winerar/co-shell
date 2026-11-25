package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CushionTest {
    private val usd = CurrencyCode("USD")
    private fun usd(units: Long) = Money(units * 100, usd)

    private fun period(month: Int): BudgetPeriod = budgetPeriod(YearMonth(2026, month), 1)
    private fun totals(month: Int, spent: Long, missing: Set<CurrencyCode> = emptySet()) =
        MonthTotals(period(month), usd(spent), usd(0), missing)

    private fun worth(money: Long, runningMonth: Int, missing: Set<CurrencyCode> = emptySet()): NetWorth {
        val running = period(runningMonth)
        val points = listOf(NetWorthPoint(running, LocalDate(2026, runningMonth, 11), usd(money), running = true))
        return NetWorth(points, emptyList(), missing)
    }

    @Test
    fun `months are the money over the average spending of full months, the running month is left out`() {
        val series = listOf(totals(8, 1_000), totals(9, 2_000), totals(10, 3_000), totals(11, 100))
        val c = cushion(worth(money = 9_000, runningMonth = 11), series)!!

        assertEquals(usd(2_000), c.monthlySpend)
        assertEquals(3, c.months)
        assertEquals(45L, c.tenths)
        assertEquals(4L, c.wholeMonths)
    }

    @Test
    fun `only the last six full months count and empty months do not pull the average down`() {
        val series = listOf(totals(1, 9_000), totals(2, 1_000), totals(3, 0), totals(4, 1_000), totals(5, 1_000), totals(6, 1_000), totals(7, 1_000), totals(8, 1_000), totals(9, 0))
        val c = cushion(worth(money = 5_000, runningMonth = 10), series)!!

        assertEquals(6, c.months)
        assertEquals(usd(1_000), c.monthlySpend)
        assertEquals(50L, c.tenths)
    }

    @Test
    fun `no full month with spending means no cushion`() {
        assertNull(cushion(worth(1_000, 11), listOf(totals(11, 500))))
        assertNull(cushion(worth(1_000, 11), listOf(totals(10, 0))))
        assertNull(cushion(worth(1_000, 11), emptyList()))
    }

    @Test
    fun `money that is not positive lasts zero months`() {
        val c = cushion(worth(money = -300, runningMonth = 11), listOf(totals(10, 1_000)))!!

        assertEquals(0L, c.tenths)
    }

    @Test
    fun `currencies without a rate are carried over from both sides`() {
        val ars = CurrencyCode("ARS")
        val eur = CurrencyCode("EUR")
        val c = cushion(worth(1_000, 11, missing = setOf(ars)), listOf(totals(10, 500, missing = setOf(eur))))!!

        assertEquals(setOf(ars, eur), c.missing)
    }
}

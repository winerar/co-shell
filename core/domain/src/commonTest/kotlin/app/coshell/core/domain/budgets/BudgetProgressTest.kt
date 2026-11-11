package app.coshell.core.domain.budgets

import app.coshell.core.domain.budgets.BudgetFixtures.amd
import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.november
import app.coshell.core.domain.budgets.BudgetFixtures.october
import app.coshell.core.domain.budgets.BudgetFixtures.today
import app.coshell.core.model.Budget
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BudgetProgressTest {
    private val home = Budget("home:2026-10", "home", october, ars(400_000))
    private val period = budgetPeriod(november)

    private fun progress(spent: Long, day: LocalDate = today, carried: Long = 0, budget: Budget = home) =
        budgetProgress(budget, period, day, Spent(ars(spent)), ars(carried))

    @Test
    fun `ahead of the pace - the board numbers`() {
        val result = progress(spent = 310_000) // 11 November: 11 of 30 days gone

        assertEquals(BudgetState.FAST, result.state)
        assertEquals(Money(146_666_67, ars), result.paceToday) // «к сегодня должно быть ≈ 147 000»
        assertEquals(ars(90_000), result.left)
        assertEquals(19, result.daysLeft)
        assertEquals(Money(4_736_84, ars), result.perDay)
        assertEquals(Money(845_454_55, ars), result.forecast)
    }

    @Test
    fun `within the pace`() {
        val result = progress(spent = 130_000, budget = home.copy(limit = ars(350_000)))

        assertEquals(BudgetState.OK, result.state) // pace 128 333: ahead by less than a tenth
        assertEquals(ars(220_000), result.left)
        assertEquals(Money(11_578_95, ars), result.perDay) // «11 600 в день»
    }

    @Test
    fun `spending up to a tenth ahead of the pace or exactly at the limit is still fine`() {
        assertEquals(BudgetState.OK, progress(spent = 220_000, day = LocalDate(2026, 11, 15)).state) // pace 200 000
        assertEquals(BudgetState.FAST, progress(spent = 220_001, day = LocalDate(2026, 11, 15)).state)
        assertEquals(BudgetState.OK, progress(spent = 400_000, day = LocalDate(2026, 11, 30)).state)
    }

    @Test
    fun `over the limit`() {
        val result = progress(spent = 415_000)

        assertEquals(BudgetState.OVER, result.state)
        assertEquals(ars(-15_000), result.left)
        assertNull(result.perDay)
    }

    @Test
    fun `no forecast in the first days`() {
        assertNull(progress(spent = 50_000, day = LocalDate(2026, 11, 2)).forecast)
        assertEquals(ars(500_000), progress(spent = 50_000, day = LocalDate(2026, 11, 3)).forecast)
    }

    @Test
    fun `on the last day there is no per-day amount and the forecast is what is spent`() {
        val result = progress(spent = 100_000, day = LocalDate(2026, 11, 30))

        assertEquals(0, result.daysLeft)
        assertNull(result.perDay)
        assertEquals(ars(100_000), result.forecast)
        assertEquals(ars(400_000), result.paceToday)
    }

    @Test
    fun `a past month has no pace - only within or over`() {
        val within = progress(spent = 390_000, day = LocalDate(2026, 12, 5))
        val over = progress(spent = 410_000, day = LocalDate(2026, 12, 5))

        assertEquals(BudgetState.OK, within.state)
        assertNull(within.paceToday)
        assertNull(within.forecast)
        assertNull(within.perDay)
        assertEquals(0, within.daysLeft)
        assertEquals(BudgetState.OVER, over.state)
    }

    @Test
    fun `a future month is untouched`() {
        val result = progress(spent = 0, day = LocalDate(2026, 10, 20))

        assertEquals(BudgetState.OK, result.state)
        assertNull(result.paceToday)
        assertEquals(30, result.daysLeft)
    }

    @Test
    fun `what was carried over changes what the month has`() {
        val more = progress(spent = 410_000, carried = 50_000, day = LocalDate(2026, 11, 30))
        val less = progress(spent = 310_000, carried = -100_000, day = LocalDate(2026, 11, 30))

        assertEquals(ars(450_000), more.available)
        assertEquals(BudgetState.OK, more.state)
        assertEquals(ars(40_000), more.left)
        assertEquals(BudgetState.OVER, less.state)
        assertEquals(ars(-10_000), less.left)
    }

    @Test
    fun `refunds above spending give no forecast and missing currencies are passed on`() {
        val refunded = progress(spent = -5_000)
        val missing = budgetProgress(home, period, today, Spent(ars(1), setOf(amd)), ars(0))

        assertNull(refunded.forecast)
        assertEquals(BudgetState.OK, refunded.state)
        assertEquals(setOf(amd), missing.missing)
    }
}

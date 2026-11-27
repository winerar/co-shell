package app.coshell.home

import app.coshell.core.domain.budgets.DailyAllowance
import app.coshell.core.domain.budgets.DailyBase
import app.coshell.core.domain.budgets.DailyPlan
import app.coshell.core.domain.budgets.budgetPeriodOf
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.home.daily.dailyUiOf
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Сегодня можно» (#769): what the card shows of a plan. */
class DailyUiTest {
    private val ars = CurrencyCode("ARS")
    private fun ars(major: Long) = Money(major * 100, ars)
    private val format = MoneyFormat.RU.copy(trimZeroFraction = true)
    private val period = budgetPeriodOf(LocalDate(2026, 11, 26), 1)

    private fun plan(spent: Long, today: Long, upcoming: Long = 0, goals: Long = 0) = DailyPlan(
        DailyBase.LIMITS, period, ars(600_000), ars(spent), ars(upcoming), ars(goals),
        DailyAllowance.of(ars(600_000), ars(spent), ars(today), ars(upcoming), ars(goals), 5),
    )

    @Test
    fun `shows the allowance, today's share and leaves out empty lines`() {
        val ui = dailyUiOf(plan(spent = 453_450, today = 2_450, upcoming = 65_000, goals = 35_000), format)
        assertEquals("9${MoneyFormat.NARROW_NBSP}800${MoneyFormat.NBSP}ARS", ui.perDay)
        assertEquals(0.25f, ui.todayFraction, 0.01f)
        assertEquals("", dailyUiOf(plan(spent = 1_000, today = 0), format).goals)
        assertEquals(5, ui.days)
    }

    @Test
    fun `over the plan shows how much over without a sign`() {
        val ui = dailyUiOf(plan(spent = 612_000, today = 1_000), format)
        assertTrue(ui.over)
        assertEquals("12${MoneyFormat.NARROW_NBSP}000${MoneyFormat.NBSP}ARS", ui.left)
    }
}

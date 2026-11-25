package app.coshell.core.domain.budgets

import app.coshell.core.model.Budget
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

class LimitAlertsTest {
    private val usd = CurrencyCode("USD")
    private val period = BudgetPeriod(YearMonth(2026, 11), LocalDate(2026, 11, 1), LocalDate(2026, 12, 1))

    private fun progress(category: String, spent: Long, today: LocalDate = LocalDate(2026, 11, 10), limit: Long = 1000) = budgetProgress(
        Budget(category, category, YearMonth(2026, 11), Money(limit, usd)),
        period, today, Spent(Money(spent, usd)), Money(0, usd),
    )

    @Test
    fun `eighty percent comes first, with what is left and the days to go`() {
        val alert = LimitAlerts.pick(listOf(progress("cafe", 600), progress("food", 820)), emptySet())!!
        assertEquals("food", alert.progress.budget.categoryId)
        assertEquals(LimitAlertKind.NEAR, alert.kind)
        assertEquals(18, alert.leftPercent)
        assertEquals(alert.progress.daysLeft + 1, alert.daysToGo)
    }

    @Test
    fun `spending ahead of the month counts when the limit would run out`() {
        val alert = LimitAlerts.pick(listOf(progress("cafe", 600), progress("food", 100)), emptySet())!!
        assertEquals("cafe", alert.progress.budget.categoryId)
        assertEquals(LimitAlertKind.PACE, alert.kind)
    }

    @Test
    fun `each limit hears of each kind once a month`() {
        val food = progress("food", 820)
        val told = setOf(LimitAlert(food, LimitAlertKind.NEAR).key)
        assertNull(LimitAlerts.pick(listOf(food), told))
        assertEquals("food:2026-11:NEAR", told.single())
    }

    @Test
    fun `over the limit, calm spending and past months say nothing`() {
        assertNull(LimitAlerts.pick(listOf(progress("a", 1200), progress("b", 100)), emptySet()))
        assertNull(LimitAlerts.pick(listOf(progress("a", 900, today = LocalDate(2026, 12, 5))), emptySet()))
    }
}

package app.coshell.core.domain.goals

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class GoalPlanTest {
    private val usd = CurrencyCode("USD")
    private fun money(major: Long) = Money(major * 100, usd)
    private val today = LocalDate(2026, 11, 26)

    @Test
    fun `counts a part of a month as one more contribution`() {
        assertEquals(8, GoalPlan.monthsLeft(today, LocalDate(2027, 7, 1)))
        assertEquals(7, GoalPlan.monthsLeft(today, LocalDate(2027, 6, 26)))
        assertEquals(1, GoalPlan.monthsLeft(today, today))
        assertEquals(1, GoalPlan.monthsLeft(today, LocalDate(2026, 12, 3)))
    }

    @Test
    fun `splits what is left over the months and rounds up`() {
        val plan = GoalPlan.of(money(2_000), money(1_250), LocalDate(2027, 6, 26), yearly = false, today = today)
        assertEquals(money(750), plan.left)
        // 750 / 7 = 107.142… → 107.15: seven contributions never fall short.
        assertEquals(107_15L, plan.perMonth?.amountMinor)
        assertFalse(plan.reached)
        assertFalse(plan.overdue)
    }

    @Test
    fun `a reached goal needs nothing more`() {
        val plan = GoalPlan.of(money(3_000), money(3_200), LocalDate(2027, 1, 1), yearly = false, today = today)
        assertTrue(plan.reached)
        assertEquals(money(0), plan.left)
        assertNull(plan.perMonth)
    }

    @Test
    fun `no deadline means no monthly amount`() {
        val plan = GoalPlan.of(money(450), money(40), null, yearly = false, today = today)
        assertEquals(money(410), plan.left)
        assertNull(plan.perMonth)
        assertFalse(plan.overdue)
    }

    @Test
    fun `a passed one-off deadline is overdue`() {
        val plan = GoalPlan.of(money(450), money(40), LocalDate(2026, 10, 1), yearly = false, today = today)
        assertTrue(plan.overdue)
        assertNull(plan.perMonth)
    }

    @Test
    fun `a yearly goal moves on to the next round`() {
        assertEquals(LocalDate(2027, 3, 15), GoalPlan.nextRound(LocalDate(2026, 3, 15), today))
        assertEquals(LocalDate(2027, 3, 15), GoalPlan.nextRound(LocalDate(2024, 3, 15), today))
        assertEquals(LocalDate(2026, 11, 26), GoalPlan.nextRound(today, today))
        val plan = GoalPlan.of(money(360), money(0), LocalDate(2026, 3, 15), yearly = true, today = today)
        assertEquals(LocalDate(2027, 3, 15), plan.deadline)
        assertFalse(plan.overdue)
        // 26 Nov → 15 Mar: 3 months and 17 days = 4 contributions of 90.
        assertEquals(money(90), plan.perMonth)
    }

    @Test
    fun `a negative balance counts as nothing saved`() {
        val plan = GoalPlan.of(money(100), money(-20), null, yearly = false, today = today)
        assertEquals(money(0), plan.saved)
        assertEquals(money(100), plan.left)
    }

    @Test
    fun `rejects an empty target`() {
        assertFailsWith<IllegalArgumentException> { GoalPlan.of(money(0), money(0), null, yearly = false, today = today) }
    }
}

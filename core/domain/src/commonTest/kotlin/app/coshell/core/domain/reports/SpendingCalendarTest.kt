package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.november
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpendingCalendarTest {
    private val period = budgetPeriod(november)
    private val today = LocalDate(2026, 11, 20)

    private fun calendar(vararg days: ReportDay) = spendingCalendar(ledger(*days), period, today)

    @Test
    fun `one huge day does not flatten the rest`() {
        val calendar = calendar(
            day(11, 2, "cafe", ars(1_000)), day(11, 3, "cafe", ars(2_000)), day(11, 4, "cafe", ars(3_000)),
            day(11, 5, "cafe", ars(4_000)), day(11, 6, "cafe", ars(5_000_000)),
        )

        val levels = calendar.days.filter { it.spent.amountMinor > 0 }.map { it.level }
        assertEquals(listOf(1, 1, 2, 3, 4), levels)
    }

    @Test
    fun `days without spending, refunds and the future have level 0`() {
        val calendar = calendar(
            day(11, 2, "cafe", ars(1_000)),
            day(11, 3, "cafe", ars(500), TransactionType.EXPENSE),
            day(11, 3, "cafe", ars(900), TransactionType.INCOME),
            day(11, 25, "cafe", ars(7_000)),
        )

        assertEquals(0, calendar.days[2].level)
        assertEquals(0, calendar.days[24].level)
        assertTrue(calendar.days[24].future)
        // Nov 1..20 minus the 2nd: 19 days up to today with nothing spent; the refund day counts as one.
        assertEquals(19, calendar.noSpendDays)
    }

    @Test
    fun `an empty month has a quiet grid and counts every past day`() {
        val calendar = calendar()

        assertEquals(30, calendar.days.size)
        assertTrue(calendar.days.all { it.level == 0 })
        assertEquals(20, calendar.noSpendDays)
    }

    @Test
    fun `every spent day gets a level from 1 to 4`() {
        val calendar = calendar(*(1..19).map { day(11, it, "cafe", ars(it * 100L)) }.toTypedArray())

        val levels = calendar.days.filter { it.spent.amountMinor > 0 }.map { it.level }
        assertTrue(levels.all { it in 1..SpendingCalendar.LEVELS })
        assertEquals(setOf(1, 2, 3, 4), levels.toSet())
    }
}

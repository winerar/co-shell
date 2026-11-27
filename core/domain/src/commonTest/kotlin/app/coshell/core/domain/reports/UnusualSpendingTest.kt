package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.categories
import app.coshell.core.domain.budgets.BudgetFixtures.october
import app.coshell.core.domain.budgets.budgetPeriod
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UnusualSpendingTest {
    private fun mark(up: Boolean, amount: Long, expected: Long, percent: Int?) =
        UsualMark(if (up) UsualDirection.ABOVE else UsualDirection.BELOW, ars(amount), ars(expected), percent)

    @Test
    fun `the biggest differences in money come first, at most three`() {
        val marks = mapOf(
            "cafe" to mark(true, 200, 100, 100),
            "taxi" to mark(false, 70, 100, -30),
            "home" to mark(true, 900, 500, 80),
            "fun" to mark(true, 50, 0, null),
            "gifts" to mark(false, 10, 40, -75),
        )
        assertEquals(listOf("home", "cafe", "fun"), unusualSpending(marks).map { it.categoryId })
    }

    @Test
    fun `small differences in percent are not unusual, new spending is`() {
        val marks = mapOf("cafe" to mark(true, 125, 100, 25), "fun" to mark(true, 50, 0, null), "taxi" to mark(false, 71, 100, -29))
        assertEquals(listOf("fun"), unusualSpending(marks).map { it.categoryId })
    }

    @Test
    fun `a finished month's recap shows what spent far more than the three months before`() {
        val history = (7..9).flatMap { m -> listOf(day(m, 5, "cafe", ars(100)), day(m, 6, "home", ars(1000))) }
        val days = history + listOf(day(10, 5, "cafe", ars(300)), day(10, 6, "home", ars(1000)))
        val recap = monthRecap(ledger(*days.toTypedArray()), categories, budgetPeriod(october), budgetPeriod(YearMonth(2026, 9)), startDay = 1)
        assertEquals(listOf("cafe"), recap.unusual.map { it.categoryId })
        assertEquals(RecapCard.UNUSUAL, recap.cards[recap.cards.indexOf(RecapCard.VS_PREVIOUS) + 1])
    }

    @Test
    fun `without the start day the recap has no unusual story`() {
        val days = (7..10).map { m -> day(m, 5, "cafe", ars(if (m == 10) 900L else 100L)) }
        val recap = monthRecap(ledger(*days.toTypedArray()), categories, budgetPeriod(october), budgetPeriod(YearMonth(2026, 9)))
        assertTrue(recap.unusual.isEmpty())
    }
}

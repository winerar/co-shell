package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.october
import app.coshell.core.domain.budgets.BudgetFixtures.usd
import app.coshell.core.domain.budgets.Spent
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.budgets.budgetProgress
import app.coshell.core.model.Budget
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LimitsTallyTest {
    private val period = budgetPeriod(october)
    private val after = LocalDate(2026, 11, 20)

    private fun limit(categoryId: String, limit: Money, spent: Money, carried: Money = Money.zero(limit.currency)) =
        budgetProgress(Budget("$categoryId:2026-10", categoryId, october, limit), period, after, Spent(spent), carried)

    @Test
    fun `within and overspent, the largest overspending by its share of the limit first`() {
        val tally = limitsTally(
            listOf(
                limit("cafe", ars(1_000), ars(1_100)),
                limit("rent", ars(500), ars(400)),
                limit("travel", usd(100), usd(150)),
                limit("home", ars(2_000), ars(2_000)),
            ),
        )!!

        assertEquals(4, tally.total)
        assertEquals(2, tally.within)
        assertEquals(listOf("travel", "cafe"), tally.over.map { it.budget.categoryId })
    }

    @Test
    fun `a limit with nothing left after a carried overspending goes first`() {
        val tally = limitsTally(
            listOf(
                limit("travel", usd(100), usd(190)),
                limit("cafe", ars(1_000), ars(10), carried = ars(-1_000)),
            ),
        )!!

        assertEquals(listOf("cafe", "travel"), tally.over.map { it.budget.categoryId })
    }

    @Test
    fun `no limits — no tally`() {
        assertNull(limitsTally(emptyList()))
    }
}

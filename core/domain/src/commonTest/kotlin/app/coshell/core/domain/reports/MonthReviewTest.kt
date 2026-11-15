package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.categories
import app.coshell.core.domain.budgets.BudgetFixtures.october
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MonthReviewTest {
    private val month = budgetPeriod(october)

    private fun review(vararg days: ReportDay) = monthReview(ledger(*days), categories, month, startDay = 1)

    @Test
    fun `spent, came in, left and the savings rate, refunds reduce spending, adjustments count nowhere`() {
        val review = review(
            day(10, 2, "cafe", ars(300)),
            day(10, 3, "cafe", ars(50), TransactionType.INCOME),
            day(10, 4, "salary", ars(1_000), TransactionType.INCOME),
            day(10, 5, SystemCategories.ADJUSTMENT_EXPENSE_ID, ars(5_000)),
            day(10, 5, SystemCategories.ADJUSTMENT_INCOME_ID, ars(7_000), TransactionType.INCOME),
        )

        assertEquals(ars(250), review.spent)
        assertEquals(ars(1_000), review.earned)
        assertEquals(ars(750), review.saved)
        assertEquals(75, review.savingsRate)
    }

    @Test
    fun `no income — no savings rate, spent more than came in — a negative one`() {
        assertNull(review(day(10, 2, "cafe", ars(300))).savingsRate)

        val over = review(day(10, 2, "cafe", ars(1_500)), day(10, 4, "salary", ars(1_000), TransactionType.INCOME))
        assertEquals(ars(-500), over.saved)
        assertEquals(-50, over.savingsRate)
    }

    @Test
    fun `the savings rate is rounded to whole percents`() {
        val review = review(day(10, 2, "cafe", ars(2)), day(10, 4, "salary", ars(3), TransactionType.INCOME))

        assertEquals(33, review.savingsRate)
    }

    @Test
    fun `the three months before give the usual spending, months before the history are left out`() {
        val review = review(day(9, 2, "cafe", ars(100)), day(10, 2, "cafe", ars(300)))

        assertEquals(1, review.usualMonths)
        assertEquals(listOf(CategoryChange("cafe", ars(300), ars(100), 200)), review.changes)
    }

    @Test
    fun `nothing before the month — nothing to compare with`() {
        val review = review(day(10, 2, "cafe", ars(300)))

        assertEquals(0, review.usualMonths)
        assertTrue(review.changes.isEmpty())
        assertNull(review.limits)
    }

    @Test
    fun `the ledger starts on the first day of the first month compared with`() {
        assertEquals(LocalDate(2026, 7, 10), reviewStart(budgetPeriod(october, 10), startDay = 10))
        assertEquals(month.start, reviewStart(month, startDay = 1, usualMonths = 0))
    }
}

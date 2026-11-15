package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.november
import app.coshell.core.domain.budgets.BudgetFixtures.today
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IncomeExpensesTest {
    @Test
    fun `savings rate is what was left in percents of income`() {
        val series = monthlySeries(ledger(day(10, 1, "salary", ars(1_620), TransactionType.INCOME), day(10, 3, "cafe", ars(1_350))), reportMonths(november, 2))

        assertEquals(17, series.first().savingsRate)
        assertNull(series.last().savingsRate, "nothing came in in November")
    }

    @Test
    fun `spending more than came in gives a negative rate`() {
        val series = monthlySeries(ledger(day(9, 1, "salary", ars(1_000), TransactionType.INCOME), day(9, 3, "rent", ars(1_100))), reportMonths(november, 3))

        assertEquals(-10, series.first().savingsRate)
    }

    @Test
    fun `the average skips the running month and months without operations`() {
        val ledger = ledger(
            day(9, 1, "salary", ars(1_000), TransactionType.INCOME),
            day(9, 2, "cafe", ars(800)),
            day(10, 1, "salary", ars(1_000), TransactionType.INCOME),
            day(10, 2, "cafe", ars(600)),
            day(11, 1, "salary", ars(5_000), TransactionType.INCOME),
        )

        val picture = incomeExpenses(monthlySeries(ledger, reportMonths(november, 6)), today)

        assertEquals(30, picture.averageRate, "(200 + 400) of 2 000")
        assertEquals(2, picture.averageMonths)
        assertFalse(picture.isEmpty)
    }

    @Test
    fun `no finished month — no average`() {
        val picture = incomeExpenses(monthlySeries(ledger(day(11, 1, "cafe", ars(10))), reportMonths(november, 1)), today)

        assertNull(picture.averageRate)
        assertEquals(0, picture.averageMonths)
        assertTrue(picture.months.single().isRunning(today))
    }

    @Test
    fun `nothing at all is empty`() {
        assertTrue(incomeExpenses(monthlySeries(ledger(), reportMonths(november, 6)), today).isEmpty)
    }
}

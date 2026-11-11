package app.coshell.core.domain.budgets

import app.coshell.core.domain.budgets.BudgetFixtures.amd
import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.categories
import app.coshell.core.domain.budgets.BudgetFixtures.november
import app.coshell.core.domain.budgets.BudgetFixtures.october
import app.coshell.core.domain.budgets.BudgetFixtures.operation
import app.coshell.core.domain.budgets.BudgetFixtures.rates
import app.coshell.core.domain.budgets.BudgetFixtures.tz
import app.coshell.core.domain.budgets.BudgetFixtures.usd
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BudgetLedgerTest {
    private fun ledger(vararg operations: Transaction) = BudgetLedger(operations.toList(), RateBook(rates), tz)
    private val cafe = setOf("cafe")

    @Test
    fun `a folder covers itself and its subcategories, archived too`() {
        assertEquals(setOf("home", "rent", "utilities"), budgetCategoryIds("home", categories))
        assertEquals(setOf("cafe"), budgetCategoryIds("cafe", categories))
    }

    @Test
    fun `spending of a folder is the sum of its categories within the period`() {
        val ledger = ledger(
            operation("home", ars(1_000), 11, 1, hour = 0),
            operation("rent", ars(300_000), 11, 3),
            operation("utilities", ars(9_000), 11, 30, hour = 23),
            operation("rent", ars(300_000), 10, 31, hour = 23),
            operation("rent", ars(300_000), 12, 1, hour = 0),
            operation("cafe", ars(5_000), 11, 4),
        )

        val spent = ledger.spent(budgetCategoryIds("home", categories), budgetPeriod(november), ars)

        assertEquals(Spent(ars(310_000)), spent)
    }

    @Test
    fun `refunds reduce spending, transfers and balance adjustments do not count`() {
        val ledger = ledger(
            operation("cafe", ars(10_000), 11, 2),
            operation("cafe", ars(4_000), 11, 3, type = TransactionType.INCOME),
            operation("cafe", ars(7_000), 11, 4, type = TransactionType.TRANSFER),
            operation(SystemCategories.ADJUSTMENT_EXPENSE_ID, ars(50_000), 11, 5),
            operation(null, ars(1_000), 11, 6),
        )

        assertEquals(ars(6_000), ledger.spent(cafe, budgetPeriod(november), ars).sum)
        assertEquals(
            Money.zero(ars),
            ledger.spent(setOf(SystemCategories.ADJUSTMENT_EXPENSE_ID), budgetPeriod(november), ars).sum,
        )
    }

    @Test
    fun `each operation is converted at the rate of its own day`() {
        val ledger = ledger(
            operation("cafe", ars(10_000), 11, 2), // 1000 ARS per USD
            operation("cafe", ars(10_000), 11, 6), // 2000 ARS per USD
            operation("cafe", usd(3), 11, 7),
        )

        assertEquals(usd(18), ledger.spent(cafe, budgetPeriod(november), usd).sum)
        assertEquals(ars(26_000), ledger.spent(cafe, budgetPeriod(november), ars).sum)
    }

    @Test
    fun `operations without a rate are left out and named`() {
        val ledger = ledger(
            operation("cafe", ars(10_000), 11, 2),
            operation("cafe", Money.ofMajor(500, currency = amd), 11, 3),
        )

        assertEquals(Spent(ars(10_000), setOf(amd)), ledger.spent(cafe, budgetPeriod(november), ars))
    }

    @Test
    fun `a period from another start day takes its own days`() {
        val ledger = ledger(operation("cafe", ars(1_000), 11, 9), operation("cafe", ars(2_000), 11, 10))

        assertEquals(ars(1_000), ledger.spent(cafe, budgetPeriod(october, startDay = 10), ars).sum)
        assertEquals(ars(2_000), ledger.spent(cafe, budgetPeriod(november, startDay = 10), ars).sum)
    }

    @Test
    fun `the hint is the average of the three months before`() {
        val ledger = ledger(
            operation("cafe", ars(1_000), 7, 15),
            operation("cafe", ars(60_000), 8, 10),
            operation("cafe", ars(90_000), 9, 10),
            operation("cafe", ars(30_000), 10, 10),
            operation("cafe", ars(500_000), 11, 2),
        )

        assertEquals(ars(60_000), ledger.averageSpent(cafe, november, ars))
    }

    @Test
    fun `months before the first record are not averaged`() {
        val ledger = ledger(operation("salary", ars(1), 10, 20, type = TransactionType.INCOME), operation("cafe", ars(30_000), 10, 25))

        assertEquals(ars(30_000), ledger.averageSpent(cafe, november, ars))
        assertEquals(ars(15_000), ledger.averageSpent(cafe, YearMonth(2026, 12), ars)) // October and empty November
    }

    @Test
    fun `no hint without history or spending`() {
        assertNull(ledger().averageSpent(cafe, november, ars))
        assertNull(ledger(operation("cafe", ars(5_000), 11, 2)).averageSpent(cafe, november, ars))
        assertNull(ledger(operation("rent", ars(5_000), 10, 2)).averageSpent(cafe, november, ars))
    }
}

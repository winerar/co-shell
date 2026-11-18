package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.categories
import app.coshell.core.domain.budgets.BudgetFixtures.november
import app.coshell.core.domain.budgets.BudgetFixtures.usd
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CategoryMonthTableTest {
    private val periods = reportMonths(november, 3) // September, October, November

    private fun table(vararg days: ReportDay) = categoryMonthTable(ledger(*days), categories, periods)

    @Test
    fun `folders open into categories, months in columns, total and average`() {
        val t = table(
            day(9, 3, "rent", ars(3_000)),
            day(10, 3, "rent", ars(3_000)),
            day(11, 3, "rent", ars(3_000)),
            day(10, 9, "home", ars(300)),
            day(11, 9, "cafe", ars(600)),
            day(9, 10, "cafe", ars(900)),
        )

        assertEquals(listOf("home", "cafe"), t.spending.map { it.categoryId })
        val home = t.spending[0]
        assertEquals(listOf(ars(3_000), ars(3_300), ars(3_000)), home.months)
        assertEquals(ars(9_300), home.total)
        assertEquals(ars(3_100), home.average)
        assertEquals(listOf("rent", "home"), home.children.map { it.categoryId }, "its own operations — the part with the folder's id")
        assertEquals(listOf(ars(900), ars(0), ars(600)), t.spending[1].months)
        assertTrue(t.spending[1].children.isEmpty())
        assertEquals(listOf(ars(3_900), ars(3_300), ars(3_600)), t.spendingTotals.months)
        assertEquals(ars(10_800), t.spendingTotals.total)
        assertEquals(ars(3_600), t.spendingTotals.average)
    }

    @Test
    fun `income is a block apart, refunds reduce spending, adjustments are not in the table`() {
        val t = table(
            day(11, 2, "cafe", ars(1_000)),
            day(11, 3, "cafe", ars(250), TransactionType.INCOME),
            day(11, 4, "salary", ars(90_000), TransactionType.INCOME),
            day(10, 4, SystemCategories.ADJUSTMENT_EXPENSE_ID, ars(500)),
        )

        assertEquals(listOf(ars(0), ars(0), ars(750)), t.spending.single().months)
        assertEquals(listOf("salary"), t.income.map { it.categoryId })
        assertEquals(ars(90_000), t.incomeTotals.total)
        assertEquals(ars(30_000), t.incomeTotals.average)
    }

    @Test
    fun `a category with nothing in any month has no row, an empty table is empty`() {
        assertTrue(table().isEmpty)
        assertEquals(ars(0), table().spendingTotals.total)
        assertEquals(3, table().spendingTotals.months.size)
    }

    @Test
    fun `a day without a rate is named and left out`() {
        val t = table(day(11, 2, "cafe", ars(10)), day(11, 3, "cafe", Money(5, CurrencyCode("USDT"))))

        assertEquals(setOf(CurrencyCode("USDT")), t.missing)
        assertEquals(ars(10), t.spending.single().total)
    }

    @Test
    fun `at least one month is needed`() {
        assertEquals(true, runCatching { categoryMonthTable(ledger(), categories, emptyList()) }.isFailure)
    }

    @Test
    fun `year summary — totals, average month, top folders, the priciest month`() {
        val s = yearSummary(
            ledger(
                day(9, 3, "rent", ars(3_000)),
                day(10, 3, "rent", ars(3_000)),
                day(11, 3, "cafe", ars(6_000)),
                day(11, 4, "home", ars(500)),
                day(10, 5, "salary", ars(20_000), TransactionType.INCOME),
                day(9, 6, "utilities", usd(1)),
            ),
            categories, periods,
        )

        assertEquals(3, s.months)
        // 1 USD = 1000 ARS on 6 Sep
        assertEquals(ars(13_500), s.spent)
        assertEquals(ars(20_000), s.earned)
        assertEquals(ars(6_500), s.saved)
        assertEquals(ars(4_500), s.averageSpent)
        assertEquals(listOf("home" to ars(7_500), "cafe" to ars(6_000)), s.topFolders)
        assertEquals(november, s.priciest?.period?.month)
    }

    @Test
    fun `year summary of an empty year`() {
        val s = yearSummary(ledger(), categories, periods)

        assertNull(s.priciest)
        assertTrue(s.topFolders.isEmpty())
        assertEquals(ars(0), s.averageSpent)
    }
}

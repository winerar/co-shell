package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.categories
import app.coshell.core.domain.budgets.BudgetFixtures.october
import app.coshell.core.domain.budgets.BudgetFixtures.usd
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals

class UsualChangesTest {
    private val month = budgetPeriod(october)
    private val before = monthsBefore(month, startDay = 1)

    /** Every one of July, August and September: rent 1 000, cafe 300, taxi 50. */
    private val usual = (7..9).flatMap { m ->
        listOf(day(m, 2, "rent", ars(1_000)), day(m, 3, "cafe", ars(300)), day(m, 4, "taxi", ars(50)))
    }

    private fun changes(vararg october: ReportDay) =
        usualChanges(ledger(*(usual + october).toTypedArray()), categories, month, before)

    @Test
    fun `the months before are the three right before, oldest first`() {
        assertEquals(listOf(7, 8, 9), before.map { it.month.month.ordinal + 1 })
    }

    @Test
    fun `largest differences first, by folder, a new one without a percent, small ones are noise`() {
        val changes = changes(
            day(10, 2, "rent", ars(1_000)),
            day(10, 3, "utilities", ars(200)),
            day(10, 4, "cafe", ars(600)),
            day(10, 5, "gift", ars(100)),
            day(10, 6, "taxi", ars(80)),
        )

        assertEquals(
            listOf(
                CategoryChange("cafe", ars(600), ars(300), 100),
                CategoryChange("home", ars(1_200), ars(1_000), 20),
                CategoryChange("gift", ars(100), ars(0), null),
            ),
            changes,
        )
    }

    @Test
    fun `spending less than usual is a change too`() {
        val changes = changes(day(10, 2, "rent", ars(1_000)), day(10, 6, "taxi", ars(50)))

        assertEquals(listOf(CategoryChange("cafe", ars(0), ars(300), -100)), changes)
        assertEquals(ars(-300), changes.single().difference)
    }

    @Test
    fun `no more than three, and none when nothing moved`() {
        val many = changes(
            day(10, 2, "rent", ars(2_000)),
            day(10, 4, "cafe", ars(900)),
            day(10, 5, "gift", ars(400)),
            day(10, 6, "taxi", ars(250)),
        )
        assertEquals(listOf("home", "cafe", "gift"), many.map { it.categoryId })

        val same = changes(day(10, 2, "rent", ars(1_000)), day(10, 3, "cafe", ars(300)), day(10, 4, "taxi", ars(50)))
        assertEquals(emptyList(), same)
    }

    @Test
    fun `more refunds than spending counts as nothing spent`() {
        val changes = changes(
            day(10, 2, "rent", ars(1_000)),
            day(10, 3, "cafe", ars(500), TransactionType.INCOME),
            day(10, 4, "taxi", ars(50)),
        )

        assertEquals(listOf(CategoryChange("cafe", ars(0), ars(300), -100)), changes)
    }

    @Test
    fun `a month with operations only in a currency without a rate still counts as history`() {
        val amd = Money.ofMajor(5_000, currency = CurrencyCode("AMD"))
        val changes = usualChanges(
            ledger(day(9, 2, "cafe", amd), day(10, 2, "cafe", usd(1))),
            categories, month, before,
        )

        assertEquals(listOf(CategoryChange("cafe", ars(1_000), ars(0), null)), changes)
    }
}

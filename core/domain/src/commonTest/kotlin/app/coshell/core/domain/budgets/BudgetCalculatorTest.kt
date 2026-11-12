package app.coshell.core.domain.budgets

import app.coshell.core.domain.budgets.BudgetFixtures.amd
import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.categories
import app.coshell.core.domain.budgets.BudgetFixtures.november
import app.coshell.core.domain.budgets.BudgetFixtures.october
import app.coshell.core.domain.budgets.BudgetFixtures.operation
import app.coshell.core.domain.budgets.BudgetFixtures.rates
import app.coshell.core.domain.budgets.BudgetFixtures.september
import app.coshell.core.domain.budgets.BudgetFixtures.today
import app.coshell.core.domain.budgets.BudgetFixtures.tz
import app.coshell.core.domain.budgets.BudgetFixtures.usd
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Budget
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.budgetId
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BudgetCalculatorTest {
    private val book = RateBook(rates)

    private fun limit(categoryId: String, from: YearMonth, amount: Money, rollover: Boolean = false, last: YearMonth? = null) =
        Budget(budgetId(categoryId, from), categoryId, from, amount, rollover = rollover, lastMonth = last)

    private fun calculator(budgets: List<Budget>, operations: List<Transaction>, startDay: Int = 1) =
        BudgetCalculator(budgets, categories, BudgetLedger(operations, book, tz), book, startDay)

    @Test
    fun `a folder limit counts all its categories, a category limit only its own`() {
        val calculator = calculator(
            budgets = listOf(limit("home", october, ars(400_000)), limit("rent", october, ars(300_000))),
            operations = listOf(operation("rent", ars(250_000), 11, 3), operation("utilities", ars(60_000), 11, 8)),
        )

        val byCategory = calculator.month(november, today).associateBy { it.budget.categoryId }

        assertEquals(setOf("home", "rent"), byCategory.keys)
        assertEquals(ars(310_000), byCategory.getValue("home").spent)
        assertEquals(BudgetState.FAST, byCategory.getValue("home").state)
        assertEquals(ars(250_000), byCategory.getValue("rent").spent)
        assertNull(calculator.progress("cafe", november, today))
    }

    @Test
    fun `a limit in another currency sees spending at the rate of each day`() {
        val calculator = calculator(
            budgets = listOf(limit("cafe", october, usd(100))),
            operations = listOf(operation("cafe", ars(20_000), 11, 2), operation("cafe", ars(20_000), 11, 6)),
        )

        val cafe = calculator.progress("cafe", november, today)!!

        assertEquals(usd(30), cafe.spent) // 20 at 1000 + 10 at 2000
        assertEquals(usd(70), cafe.left)
    }

    @Test
    fun `without rollover every month starts from the limit`() {
        val calculator = calculator(
            budgets = listOf(limit("cafe", october, ars(80_000))),
            operations = listOf(operation("cafe", ars(50_000), 10, 5)),
        )

        val cafe = calculator.progress("cafe", november, today)!!

        assertEquals(Money.zero(ars), cafe.carried)
        assertEquals(ars(80_000), cafe.available)
    }

    @Test
    fun `rollover carries the rest and the overspending month after month`() {
        val calculator = calculator(
            budgets = listOf(limit("cafe", september, ars(80_000), rollover = true)),
            operations = listOf(
                operation("cafe", ars(50_000), 9, 5), // +30 000
                operation("cafe", ars(125_000), 10, 5), // 110 000 available → −15 000
                operation("cafe", ars(20_000), 11, 5),
            ),
        )

        assertEquals(Money.zero(ars), calculator.progress("cafe", september, today)!!.carried)
        assertEquals(ars(30_000), calculator.progress("cafe", october, today)!!.carried)
        val now = calculator.progress("cafe", november, today)!!
        assertEquals(ars(-15_000), now.carried)
        assertEquals(ars(65_000), now.available)
        assertEquals(ars(45_000), now.left)
    }

    @Test
    fun `rollover turned on starts from that month, turned off drops the carried amount`() {
        val operations = listOf(operation("cafe", ars(50_000), 9, 5), operation("cafe", ars(50_000), 10, 5))
        val turnedOn = calculator(
            listOf(limit("cafe", september, ars(80_000)), limit("cafe", october, ars(80_000), rollover = true)),
            operations,
        )
        val turnedOff = calculator(
            listOf(limit("cafe", september, ars(80_000), rollover = true), limit("cafe", november, ars(80_000))),
            operations,
        )

        assertEquals(Money.zero(ars), turnedOn.progress("cafe", october, today)!!.carried)
        assertEquals(ars(30_000), turnedOn.progress("cafe", november, today)!!.carried)
        assertEquals(ars(30_000), turnedOff.progress("cafe", october, today)!!.carried)
        assertEquals(Money.zero(ars), turnedOff.progress("cafe", november, today)!!.carried)
    }

    @Test
    fun `a removed limit breaks the rollover row`() {
        val calculator = calculator(
            budgets = listOf(
                limit("cafe", YearMonth(2026, 8), ars(80_000), rollover = true, last = september),
                limit("cafe", november, ars(80_000), rollover = true),
            ),
            operations = listOf(operation("cafe", ars(10_000), 9, 5)),
        )

        assertNull(calculator.progress("cafe", october, today))
        assertEquals(Money.zero(ars), calculator.progress("cafe", november, today)!!.carried)
    }

    @Test
    fun `the carried amount follows the limit into a new currency`() {
        val calculator = calculator(
            budgets = listOf(
                limit("cafe", october, ars(80_000), rollover = true),
                limit("cafe", november, usd(100), rollover = true),
            ),
            operations = listOf(operation("cafe", ars(50_000), 10, 5)),
        )

        assertEquals(usd(30), calculator.progress("cafe", november, today)!!.carried) // 30 000 ARS on 1 November, at 1000
    }

    @Test
    fun `the month follows the family's start day`() {
        val calculator = calculator(
            budgets = listOf(limit("cafe", october, ars(80_000))),
            operations = listOf(operation("cafe", ars(1_000), 11, 9), operation("cafe", ars(2_000), 11, 10)),
            startDay = 10,
        )

        assertEquals(ars(1_000), calculator.progress("cafe", october, today)!!.spent)
        val now = calculator.progress("cafe", november, today)!!
        assertEquals(ars(2_000), now.spent)
        assertEquals(LocalDate(2026, 11, 10), now.period.start)
        assertEquals(28, now.daysLeft)
    }

    @Test
    fun `month summary in the main currency does not count a category inside a limited folder twice`() {
        val calculator = calculator(
            budgets = listOf(
                limit("home", october, ars(400_000)),
                limit("rent", october, ars(300_000)),
                limit("cafe", october, usd(100)),
            ),
            operations = listOf(
                operation("rent", ars(200_000), 11, 3), // 200 USD at 1000
                operation("utilities", ars(40_000), 11, 8), // 20 USD at 2000
                operation("cafe", usd(55), 11, 9),
                operation("salary", ars(900_000), 11, 9), // not in any limit
            ),
        )

        val summary = calculator.summary(november, today, usd)

        assertEquals(usd(300), summary.planned) // 400 000 ARS at today's 2000 + 100
        assertEquals(usd(275), summary.spent)
        assertEquals(usd(25), summary.left)
        assertEquals(usd(750), summary.forecast) // 275 × 30 / 11
        assertEquals(emptySet(), summary.missing)
    }

    @Test
    fun `summary counts a category limit when its folder has none, adds carried amounts and names missing rates`() {
        val calculator = calculator(
            budgets = listOf(
                limit("rent", october, ars(300_000), rollover = true),
                limit("cafe", october, Money.ofMajor(40_000, currency = amd)),
            ),
            operations = listOf(operation("rent", ars(250_000), 10, 3), operation("home", ars(999), 11, 3)),
        )

        val summary = calculator.summary(november, today, ars)

        assertEquals(ars(350_000), summary.planned)
        assertEquals(Money.zero(ars), summary.spent)
        assertEquals(setOf(amd), summary.missing)
    }

    @Test
    fun `summary of a past month has no forecast and of an empty month is zero`() {
        val calculator = calculator(listOf(limit("cafe", october, ars(80_000))), listOf(operation("cafe", ars(90_000), 10, 3)))

        val past = calculator.summary(october, today, ars)
        val empty = calculator.summary(september, today, ars)

        assertEquals(ars(-10_000), past.left)
        assertNull(past.forecast)
        assertEquals(Money.zero(ars), empty.planned)
        assertEquals(emptyList(), calculator.month(september, today))
    }
}

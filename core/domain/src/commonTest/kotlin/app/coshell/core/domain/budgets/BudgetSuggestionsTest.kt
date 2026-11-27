package app.coshell.core.domain.budgets

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.categories
import app.coshell.core.domain.budgets.BudgetFixtures.november
import app.coshell.core.domain.budgets.BudgetFixtures.operation
import app.coshell.core.domain.budgets.BudgetFixtures.rates
import app.coshell.core.domain.budgets.BudgetFixtures.tz
import app.coshell.core.domain.budgets.BudgetFixtures.usd
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BudgetSuggestionsTest {
    private fun ledger(vararg operations: Transaction) = BudgetLedger(operations.toList(), RateBook(rates), tz)

    private fun suggest(ledger: BudgetLedger, cats: List<Category> = categories) = suggestBudgetLimits(ledger, cats, november, ars)

    @Test
    fun `a folder is offered once, with its subcategories and archived ones counted in`() {
        val suggestions = suggest(
            ledger(
                operation("rent", ars(300_000), 10, 3),
                operation("utilities", ars(30_000), 10, 5),
                operation("home", ars(30_000), 10, 7),
            ),
        )

        assertEquals(listOf(BudgetSuggestion("home", ars(360_000))), suggestions)
    }

    @Test
    fun `the average runs over the months that have records, the biggest limit first`() {
        val suggestions = suggest(
            ledger(
                operation("cafe", ars(60_000), 9, 10),
                operation("cafe", ars(90_000), 10, 10),
                operation("rent", ars(400_000), 9, 3),
                operation("rent", ars(400_000), 10, 3),
            ),
        )

        // September and October only: August has nothing recorded before them, so it is not a month of zero spending.
        assertEquals(listOf("home", "cafe"), suggestions.map { it.categoryId })
        assertEquals(ars(400_000), suggestions[0].limit)
        assertEquals(ars(75_000), suggestions[1].limit)
    }

    @Test
    fun `income, nothing spent and no history offer nothing`() {
        assertTrue(suggest(ledger()).isEmpty())
        assertTrue(suggest(ledger(operation("salary", ars(500_000), 10, 1, type = TransactionType.INCOME))).isEmpty())
        // Spent only in the month being planned: that is not history.
        assertTrue(suggest(ledger(operation("cafe", ars(5_000), 11, 2))).isEmpty())
    }

    @Test
    fun `an archived category and a subcategory alone are not offered`() {
        val cats = categories + Category("old", "Старое", CategoryType.EXPENSE, archived = true)
        val suggestions = suggest(ledger(operation("old", ars(10_000), 10, 2), operation("cafe", ars(10_000), 10, 3)), cats)

        assertEquals(listOf("cafe"), suggestions.map { it.categoryId })
    }

    @Test
    fun `a limit is a round figure of whole units`() {
        assertEquals(ars(370_000), roundedLimit(ars(371_840)))
        assertEquals(ars(78_000), roundedLimit(ars(78_400)))
        assertEquals(ars(1_200_000), roundedLimit(ars(1_236_999)))
        assertEquals(ars(1_500), roundedLimit(ars(1_450)))
        assertEquals(ars(999), roundedLimit(ars(999)))
        assertEquals(usd(38), roundedLimit(Money(3_780, usd)))
        assertEquals(usd(1), roundedLimit(Money(12, usd)))
    }

    private fun usd(major: Long) = BudgetFixtures.usd(major)
}

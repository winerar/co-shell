package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import kotlin.test.Test
import kotlin.test.assertEquals

class CategorySpendingTest {
    private val currency = CurrencyCode("ARS")

    private fun sums(vararg pairs: Pair<String, Long>) = CategorySums(pairs.associate { it.first to ars(it.second) }, emptySet())

    @Test
    fun `every spending category by amount with the period before, refunds-only left out`() {
        val rows = categorySpending(sums("cafe" to 300, "rent" to 700, "gift" to -50), sums("cafe" to 100), currency)

        assertEquals(listOf("rent" to 70, "cafe" to 30), rows.map { it.categoryId to it.percent })
        assertEquals(listOf(ars(0), ars(100)), rows.map { it.previous })
        assertEquals(ars(200), rows[1].growth)
    }

    @Test
    fun `by growth puts the biggest rise first`() {
        val rows = categorySpending(sums("cafe" to 300, "rent" to 700), sums("cafe" to 100, "rent" to 690), currency, CategoryOrder.BY_GROWTH)

        assertEquals(listOf("cafe", "rent"), rows.map { it.categoryId })
    }

    @Test
    fun `folder totals put subcategories into their folder`() {
        val categories = listOf(
            Category("home", "Дом", CategoryType.EXPENSE),
            Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
        )

        val totals = spendingByFolder(sums("rent" to 300, "home" to 40, "cafe" to 10), categories, currency)

        assertEquals(mapOf("home" to ars(340), "cafe" to ars(10)), totals)
    }
}

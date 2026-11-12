package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SpendingBreakdownTest {
    private val currency = CurrencyCode("ARS")
    private val folder = listOf(
        Category("home", "Дом", CategoryType.EXPENSE),
        Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
        Category("utilities", "Коммуналка", CategoryType.EXPENSE, parentId = "home", archived = true),
    )
    private val plain = (1..7).map { Category("c$it", "Категория $it", CategoryType.EXPENSE) }

    private fun sums(vararg pairs: Pair<String, Long>) = CategorySums(pairs.associate { it.first to ars(it.second) }, emptySet())

    @Test
    fun `subcategories go into their folder with the folder's composition`() {
        val breakdown = spendingBreakdown(sums("rent" to 300, "utilities" to 60, "home" to 40, "c1" to 600), folder + plain, currency)

        assertEquals(ars(1_000), breakdown.total)
        assertEquals(listOf("c1" to 60, "home" to 40), breakdown.slices.map { it.categoryId to it.percent })
        val home = breakdown.slices[1]
        assertEquals(ars(400), home.amount)
        assertEquals(listOf("rent" to 75, "utilities" to 15, "home" to 10), home.parts.map { it.categoryId to it.percent })
        assertEquals(emptyList(), breakdown.slices[0].parts)
        assertNull(breakdown.other)
    }

    @Test
    fun `everything after the five largest is other`() {
        val breakdown = spendingBreakdown(sums("c1" to 70, "c2" to 60, "c3" to 50, "c4" to 40, "c5" to 30, "c6" to 20, "c7" to 10), plain, currency)

        assertEquals(listOf("c1", "c2", "c3", "c4", "c5"), breakdown.slices.map { it.categoryId })
        val other = breakdown.other!!
        assertEquals(ars(30), other.amount)
        assertEquals(listOf("c6", "c7"), other.slices.map { it.categoryId })
        assertEquals(100, breakdown.slices.sumOf { it.percent } + other.percent)
    }

    @Test
    fun `other of a single slice shows that slice instead`() {
        val breakdown = spendingBreakdown(sums("c1" to 60, "c2" to 50, "c3" to 40, "c4" to 30, "c5" to 20, "c6" to 10), plain, currency)

        assertEquals(6, breakdown.slices.size)
        assertNull(breakdown.other)
    }

    @Test
    fun `a category with more refunds than spending is not drawn but lowers the total`() {
        val breakdown = spendingBreakdown(sums("c1" to 100, "c2" to -20), plain, currency)

        assertEquals(ars(80), breakdown.total)
        assertEquals(listOf("c1" to 100), breakdown.slices.map { it.categoryId to it.percent })
    }

    @Test
    fun `no spending is an empty breakdown`() {
        val breakdown = spendingBreakdown(sums(), plain, currency)

        assertEquals(true, breakdown.isEmpty)
        assertEquals(ars(0), breakdown.total)
    }
}

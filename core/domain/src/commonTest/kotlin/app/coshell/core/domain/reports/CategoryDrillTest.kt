package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CategoryDrillTest {
    private val currency = CurrencyCode("ARS")

    private val categories = listOf(
        Category("home", "Дом", CategoryType.EXPENSE),
        Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
        Category("power", "Свет", CategoryType.EXPENSE, parentId = "home"),
        Category("cafe", "Кафе", CategoryType.EXPENSE),
        Category("taxi", "Такси", CategoryType.EXPENSE),
    )

    private fun sums(vararg pairs: Pair<String, Long>) = CategorySums(pairs.associate { it.first to ars(it.second) }, emptySet())

    @Test
    fun `a folder adds up its subcategories and its own operations, percent of all spending`() {
        val drill = categoryDrill(sums("rent" to 262, "power" to 28, "cafe" to 400, "taxi" to 70), "home", categories, currency)!!

        assertEquals(ars(290), drill.amount)
        assertEquals(38, drill.percent)
        assertEquals(listOf("rent" to 90, "power" to 10), drill.parts.map { it.categoryId to it.percent })
    }

    @Test
    fun `a small folder that would be in Другое on the tab is still found`() {
        val many = sums("cafe" to 900, "taxi" to 800, "a" to 700, "b" to 600, "c" to 500, "d" to 400, "rent" to 1)

        val drill = categoryDrill(many, "home", categories, currency)!!

        assertEquals(ars(1), drill.amount)
        assertEquals(listOf("rent"), drill.parts.map { it.categoryId })
    }

    @Test
    fun `a subcategory stands alone with its percent among all categories`() {
        val drill = categoryDrill(sums("rent" to 300, "power" to 100, "cafe" to 600), "power", categories, currency)!!

        assertEquals(ars(100), drill.amount)
        assertEquals(10, drill.percent)
        assertEquals(emptyList(), drill.parts)
    }

    @Test
    fun `a plain category has no parts, refunds-only — nothing`() {
        assertEquals(emptyList(), categoryDrill(sums("cafe" to 60, "taxi" to 40), "cafe", categories, currency)!!.parts)
        assertNull(categoryDrill(sums("cafe" to -60, "taxi" to 40), "cafe", categories, currency))
    }
}

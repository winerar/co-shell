package app.coshell.reports

import app.coshell.reports.ReportsUiState.CategoryRow
import app.coshell.reports.all.RowOrder
import app.coshell.reports.all.orderRows
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportCategoriesOrderTest {
    private fun row(id: String, name: String, amount: Long, growth: Long, parent: String? = null) =
        CategoryRow(id, name, null, null, amount.toString(), 0, 0f, null, null, growth, amount, parent)

    private val rows = listOf(
        row("cafe", "Кафе", 300, 200),
        row("rent", "Аренда", 500, -10, parent = "home"),
        row("home", "Дом", 40, 0),
        row("power", "Свет", 100, 50, parent = "home"),
    )

    private fun ids(order: RowOrder, query: String = "") = orderRows(rows, query, order) { it.name!! }.map { it.row.categoryId to it.inner }

    @Test
    fun `by amount and by growth are flat`() {
        assertEquals(listOf("rent", "cafe", "power", "home"), ids(RowOrder.AMOUNT).map { it.first })
        assertEquals(listOf("cafe", "power", "home", "rent"), ids(RowOrder.GROWTH).map { it.first })
    }

    @Test
    fun `groups put the parent first, then its subcategories, groups by total`() {
        assertEquals(
            listOf("home" to false, "rent" to true, "power" to true, "cafe" to false),
            ids(RowOrder.GROUPS),
        )
    }

    @Test
    fun `search matches any part of the name in any case`() {
        assertEquals(listOf("cafe" to false), ids(RowOrder.AMOUNT, "КАФ"))
    }
}

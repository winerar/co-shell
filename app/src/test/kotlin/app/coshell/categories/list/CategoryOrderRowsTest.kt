package app.coshell.categories.list

import app.coshell.core.domain.order.ManualOrder.Place
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryOrderRowsTest {
    private fun item(id: String) = CategoriesUiState.Item(id, id, null)

    // food (shop, cafe), taxi, home (rent)
    private val rows = orderRows(
        listOf(
            CategoriesUiState.Group(item("food"), listOf(item("shop"), item("cafe")), false),
            CategoriesUiState.Group(item("taxi"), emptyList(), false),
            CategoriesUiState.Group(item("home"), listOf(item("rent")), false),
            CategoriesUiState.Group(item("adj"), emptyList(), false),
        ),
        fixed = { it == "adj" },
    )

    @Test
    fun `rows put subcategories under their parent and leave the fixed one out`() {
        assertEquals(listOf("food", "shop", "cafe", "taxi", "home", "rent"), rows.map { it.id })
        assertEquals(listOf(null, "food", "food", null, null, "home"), rows.map { it.parentId })
    }

    @Test
    fun `dropped between subcategories it joins them`() {
        // Others without taxi: food, shop, cafe, home, rent — index 2 is before cafe.
        assertEquals(Place("food", 1), dropPlace(rows, "taxi", 2, null))
    }

    @Test
    fun `a subcategory dropped among top rows becomes separate`() {
        // Others without cafe: food, shop, taxi, home, rent — index 3 is before home.
        assertEquals(Place(null, 2), dropPlace(rows, "cafe", 3, null))
    }

    @Test
    fun `dropped on the middle of a category it goes inside at the end`() {
        assertEquals(Place("home", 1), dropPlace(rows, "taxi", 0, "home"))
    }

    @Test
    fun `a parent dropped inside another group goes after it`() {
        // Others without food and its children: taxi, home, rent — index 2 is before rent.
        assertEquals(Place(null, 2), dropPlace(rows, "food", 2, null))
    }
}

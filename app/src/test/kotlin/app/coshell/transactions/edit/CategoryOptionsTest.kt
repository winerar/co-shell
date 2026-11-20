package app.coshell.transactions.edit

import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.transactions.edit.TransactionEditUiState.CategoryItem
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryOptionsTest {
    private fun item(id: String) = CategoryItem(id, id.replaceFirstChar { it.uppercase() })

    // «Еда» (продукты, кафе), «Дом» (аренда, свет), and 8 plain: c1..c8 — 14 to pick, the two parents included.
    private val groups = listOf(
        CategoryGroup(item("еда"), listOf(item("продукты"), item("кафе"))),
        CategoryGroup(item("дом"), listOf(item("аренда"), item("свет"))),
    ) + (1..8).map { CategoryGroup(item("c$it"), emptyList()) }

    private fun ids(list: List<CategoryItem>) = list.map { it.id }

    @Test
    fun `frequent puts recent first, then leaves in tree order, parents last`() {
        val frequent = frequentCategories(groups, recent = listOf("c5", "кафе"), selectedId = null)
        assertEquals(listOf("c5", "кафе", "продукты", "аренда", "свет", "c1", "c2"), ids(frequent))
        assertEquals("еда", frequentCategories(groups, recent = listOf("еда"), selectedId = null).first().id)
    }

    @Test
    fun `a category picked from all takes the last place`() {
        val frequent = frequentCategories(groups, recent = emptyList(), selectedId = "c8")
        assertEquals(7, frequent.size)
        assertEquals("c8", frequent.last().id)
        assertEquals(ids(frequentCategories(groups, emptyList(), null)), ids(frequentCategories(groups, emptyList(), "кафе")))
    }

    @Test
    fun `everything fitting in eight places is shown without all`() {
        val few = groups.take(1) + groups.drop(2).take(5) // «Еда» + 2 subcategories + 5 plain = 8
        assertEquals(8, frequentCategories(few, emptyList(), null).size)
        assertEquals(8, few.leafCount)
        assertEquals(14, groups.leafCount)
    }

    @Test
    fun `choice lines nest subcategories under their parent in the screen order`() {
        val lines = categoryChoiceOptions(groups, "С подкатегориями")
        assertEquals(groups.leafCount, lines.size)
        assertEquals(listOf("еда", "продукты", "кафе", "дом", "аренда", "свет", "c1"), lines.take(7).map { it.value })
        assertEquals(listOf(false, true, true, false, true, true, false), lines.take(7).map { it.indent })
        assertEquals(listOf("С подкатегориями", null, null, "С подкатегориями"), lines.take(4).map { it.subtitle })
        assertEquals(true, lines.all { it.group == null && it.hasBadge })
    }

    @Test
    fun `a subcategory of the same name is nested under its parent`() {
        val same = CategoryGroup(CategoryItem("kids", "Дети"), listOf(CategoryItem("kids2", "Дети")))
        val other = CategoryGroup(CategoryItem("taxi", "Такси"), emptyList())
        val lines = categoryChoiceOptions(listOf(same, other), "С подкатегориями")
        assertEquals(listOf("kids", "kids2", "taxi"), lines.map { it.value })
        assertEquals(listOf(false, true, false), lines.map { it.indent })
        assertEquals(listOf("С подкатегориями", null, null), lines.map { it.subtitle })
    }
}

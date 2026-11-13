package app.coshell.transactions.edit

import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.transactions.edit.TransactionEditUiState.CategoryItem
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryOptionsTest {
    private fun item(id: String) = CategoryItem(id, id.replaceFirstChar { it.uppercase() })

    // «Еда» (продукты, кафе), «Дом» (аренда, свет), and 8 folderless: c1..c8 — 12 leaves.
    private val groups = listOf(
        CategoryGroup(item("еда"), listOf(item("продукты"), item("кафе"))),
        CategoryGroup(item("дом"), listOf(item("аренда"), item("свет"))),
    ) + (1..8).map { CategoryGroup(item("c$it"), emptyList()) }

    private fun ids(list: List<CategoryItem>) = list.map { it.id }

    @Test
    fun `frequent puts recent first, then leaves in tree order, folders only when used`() {
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
        val few = groups.take(2) + groups.drop(2).take(4) // 8 leaves
        assertEquals(8, frequentCategories(few, emptyList(), null).size)
        assertEquals(8, few.leafCount)
        assertEquals(12, groups.leafCount)
    }

    @Test
    fun `shelves go folderless first, then by folder`() {
        val shelves = categoryShelves(groups)
        assertEquals(listOf(null, "Еда", "Дом"), shelves.map { it.title })
        assertEquals(8, shelves.first().items.size)
    }

    @Test
    fun `search keeps matches and a matching folder keeps all its children`() {
        assertEquals(listOf("Еда" to listOf("кафе")), categoryShelves(groups, " каф").map { it.title to ids(it.items) })
        assertEquals(listOf("Еда" to listOf("продукты", "кафе")), categoryShelves(groups, "ЕДА").map { it.title to ids(it.items) })
        assertEquals(emptyList<CategoryShelf>(), categoryShelves(groups, "нет такого"))
    }
}

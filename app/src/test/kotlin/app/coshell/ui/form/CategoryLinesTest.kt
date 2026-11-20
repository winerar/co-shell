package app.coshell.ui.form

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryLinesTest {
    private fun line(id: String, name: String, parent: Pair<String, String>? = null) =
        CategoryLine(id, name, parent?.second, "cart", 0xFF00AA00, parent?.first)

    private fun lines(vararg all: CategoryLine) = categoryLines(all.toList(), "С подкатегориями")

    @Test
    fun `subcategories follow their parent, indented, in the given order`() {
        val food = "food" to "Еда"
        val result = lines(line("tea", "Чай"), line("food", "Еда"), line("shop", "Продукты", food), line("cafe", "Кафе", food))
        assertEquals(listOf("tea", "food", "shop", "cafe"), result.map { it.value })
        assertEquals(listOf(false, false, true, true), result.map { it.indent })
        assertEquals(listOf(null, "С подкатегориями", null, null), result.map { it.subtitle })
        assertEquals(listOf<String?>(null, null, null, null), result.map { it.group })
    }

    @Test
    fun `one subcategory of the same name is nested, not a double`() {
        val kids = "kids" to "Дети"
        val result = lines(line("kids", "Дети"), line("kids2", "Дети", kids))
        assertEquals(listOf("kids", "kids2"), result.map { it.value })
        assertEquals(listOf(false, true), result.map { it.indent })
    }

    @Test
    fun `subcategories whose parent cannot be picked keep its name as a caption, after the rest`() {
        val home = "home" to "Дом"
        val result = lines(line("repair", "Ремонт", home), line("taxi", "Такси"))
        assertEquals(listOf("taxi", "repair"), result.map { it.value })
        assertEquals(listOf(null, "Дом"), result.map { it.group })
        assertEquals(listOf(false, false), result.map { it.indent })
    }

    @Test
    fun `every line has a badge and keeps its icon and colour`() {
        val one = lines(line("a", "А")).single()
        assertTrue(one.hasBadge)
        assertEquals("cart", one.icon)
        assertEquals(0xFF00AA00, one.color)
    }

    @Test
    fun `search finds a category by its parent name`() {
        val food = "food" to "Еда"
        val result = lines(line("shop", "Продукты", food), line("cafe", "Кафе", food), line("taxi", "Такси"))
        assertEquals(listOf("shop", "cafe"), ChoiceSearch.filter(result, "еда").map { it.value })
    }
}

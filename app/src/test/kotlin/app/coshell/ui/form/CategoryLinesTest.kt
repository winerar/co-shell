package app.coshell.ui.form

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryLinesTest {
    private fun line(id: String, name: String, parent: String? = null) = CategoryLine(id, name, parent, "cart", 0xFF00AA00)

    @Test
    fun `a folder with several categories is a caption, the rest go under no group after it`() {
        val lines = categoryLines(
            listOf(line("tea", "Чай"), line("shop", "Продукты", "Еда"), line("cafe", "Кафе", "Еда")),
            "Без группы",
        )
        assertEquals(listOf("shop", "cafe", "tea"), lines.map { it.value })
        assertEquals(listOf("Еда", "Еда", "Без группы"), lines.map { it.group })
    }

    @Test
    fun `a folder of one is the category itself without a double`() {
        val lines = categoryLines(listOf(line("kids", "Дети", "Дети"), line("repair", "Ремонт", "Дом")), "Без группы")
        assertEquals(listOf("Дети", "Ремонт"), lines.map { it.label })
        assertEquals(listOf(null, "Дом"), lines.map { it.subtitle })
        assertEquals(listOf("Без группы"), lines.map { it.group }.distinct())
    }

    @Test
    fun `every line has a badge and keeps its icon and colour`() {
        val one = categoryLines(listOf(line("a", "А")), "Без группы").single()
        assertTrue(one.hasBadge)
        assertEquals("cart", one.icon)
        assertEquals(0xFF00AA00, one.color)
    }

    @Test
    fun `search finds a category by its folder name`() {
        val lines = categoryLines(listOf(line("shop", "Продукты", "Еда"), line("cafe", "Кафе", "Еда"), line("taxi", "Такси")), "Без группы")
        assertEquals(listOf("shop", "cafe"), ChoiceSearch.filter(lines, "еда").map { it.value })
    }
}

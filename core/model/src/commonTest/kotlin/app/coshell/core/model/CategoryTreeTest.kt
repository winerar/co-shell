package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CategoryTreeTest {
    private fun cat(id: String, parent: String? = null, order: Int = 0, name: String = id) =
        Category(id, name, CategoryType.EXPENSE, parentId = parent, sortOrder = order)

    @Test
    fun groupsChildrenUnderFolders() {
        val tree = buildCategoryTree(
            listOf(cat("food", order = 1), cat("cafe", "food", name = "Кафе"), cat("shop", "food", name = "Магазин"), cat("car")),
        )
        assertEquals(listOf("car", "food"), tree.map { it.category.id })
        assertFalse(tree[0].isFolder)
        assertTrue(tree[1].isFolder)
        assertEquals(listOf("cafe", "shop"), tree[1].children.map { it.id })
    }

    @Test
    fun orphanSubcategoryIsShownAtTopLevel() {
        val tree = buildCategoryTree(listOf(cat("cafe", parent = "gone")))
        assertEquals(listOf("cafe"), tree.map { it.category.id })
    }
}

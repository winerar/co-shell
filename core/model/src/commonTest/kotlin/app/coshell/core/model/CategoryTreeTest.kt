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
            CATEGORY_ORDER,
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

    @Test
    fun ordersAlphabeticallyIgnoringSortOrderAndKeepsAdjustmentLast() {
        val adjustment = SystemCategories.adjustment(CategoryType.EXPENSE)
        val tree = buildCategoryTree(
            listOf(adjustment, cat("t", name = "Такси", order = 0), cat("a", name = "аптека", order = 5), cat("e", name = "Ёлка", order = 1)),
            CATEGORY_ORDER,
        )
        assertEquals(listOf("a", "e", "t", adjustment.id), tree.map { it.category.id })
    }

    @Test
    fun manualOrderGoesBySortOrderAndKeepsAdjustmentLast() {
        val adjustment = SystemCategories.adjustment(CategoryType.EXPENSE)
        val tree = buildCategoryTree(
            listOf(adjustment, cat("t", name = "Такси", order = 0), cat("a", name = "аптека", order = 5), cat("e", name = "Ёлка", order = 1)),
            categoryOrder(manual = true),
        )
        assertEquals(listOf("t", "e", "a", adjustment.id), tree.map { it.category.id })
    }

    @Test
    fun withoutOrderTheListOrderIsKept() {
        val tree = buildCategoryTree(listOf(cat("b"), cat("b2", "b"), cat("b1", "b"), cat("a")))
        assertEquals(listOf("b", "a"), tree.map { it.category.id })
        assertEquals(listOf("b2", "b1"), tree[0].children.map { it.id })
    }
}

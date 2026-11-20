package app.coshell.core.domain.order

import app.coshell.core.domain.Reorder
import app.coshell.core.domain.order.ManualOrder.Place
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.SystemCategories
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualOrderTest {
    private val usd = CurrencyCode("USD")
    private fun acc(id: String, order: Int = 0) = Account(id, id, usd, sortOrder = order)
    private fun cat(id: String, parent: String? = null, order: Int = 0) =
        Category(id, id, CategoryType.EXPENSE, parentId = parent, sortOrder = order)

    @Test
    fun firstAccountMoveStoresEveryPlaceOnScreen() {
        val shown = listOf(acc("a", 7), acc("b", 3), acc("c", 0))
        val changed = ManualOrder.moveAccount(shown, from = 2, to = 0, manual = false).associate { it.id to it.sortOrder }
        assertEquals(mapOf("c" to Reorder.STEP, "a" to 2 * Reorder.STEP, "b" to 3 * Reorder.STEP), changed)
    }

    @Test
    fun laterAccountMoveTouchesOneRow() {
        val shown = listOf(acc("a", 1024), acc("b", 2048), acc("c", 3072))
        val changed = ManualOrder.moveAccount(shown, from = 2, to = 0, manual = true)
        assertEquals(listOf("c"), changed.map { it.id })
        assertTrue(changed.single().sortOrder < 1024)
    }

    @Test
    fun categoryGoesInsideAnotherAtTheEnd() {
        val shown = listOf(cat("food", order = 1024), cat("shop", "food", 1024), cat("bakery", order = 2048))
        val changed = ManualOrder.moveCategory(shown, "bakery", Place("food", 1), manual = true)
        val bakery = changed.single { it.id == "bakery" }
        assertEquals("food", bakery.parentId)
        assertTrue(bakery.sortOrder > 1024)
    }

    @Test
    fun subcategoryComesOutToTheTop() {
        val shown = listOf(cat("food", order = 1024), cat("shop", "food", 1024), cat("cafe", "food", 2048), cat("taxi", order = 2048))
        val changed = ManualOrder.moveCategory(shown, "cafe", Place(null, 1), manual = true)
        val cafe = changed.single { it.id == "cafe" }
        assertEquals(null, cafe.parentId)
        assertTrue(cafe.sortOrder in 1025..2047)
    }

    @Test
    fun firstCategoryMoveNumbersEveryGroupAsShown() {
        val shown = listOf(cat("b", order = 5), cat("b1", "b", 9), cat("b2", "b", 1), cat("a", order = 0))
        val changed = ManualOrder.moveCategory(shown, "a", Place(null, 0), manual = false).associate { it.id to it.sortOrder }
        assertEquals(mapOf("a" to Reorder.STEP, "b" to 2 * Reorder.STEP, "b1" to Reorder.STEP, "b2" to 2 * Reorder.STEP), changed)
    }

    @Test
    fun aCategoryWithSubcategoriesStaysOnTop() {
        val shown = listOf(cat("food"), cat("shop", "food"), cat("home"))
        assertEquals(emptyList(), ManualOrder.moveCategory(shown, "food", Place("home", 0), manual = true))
    }

    @Test
    fun noNestingTwoLevelsDeepAndAdjustmentStays() {
        val adjustment = SystemCategories.adjustment(CategoryType.EXPENSE)
        val shown = listOf(cat("food"), cat("shop", "food"), cat("taxi"), adjustment)
        assertEquals(emptyList(), ManualOrder.moveCategory(shown, "taxi", Place("shop", 0), manual = true))
        assertEquals(emptyList(), ManualOrder.moveCategory(shown, adjustment.id, Place(null, 0), manual = true))
        assertEquals(emptyList(), ManualOrder.moveCategory(shown, "taxi", Place(adjustment.id, 0), manual = true))
    }

    @Test
    fun droppingInPlaceSavesNothing() {
        val shown = listOf(cat("a", order = 1024), cat("b", order = 2048))
        assertEquals(emptyList(), ManualOrder.moveCategory(shown, "b", Place(null, 1), manual = true))
    }
}

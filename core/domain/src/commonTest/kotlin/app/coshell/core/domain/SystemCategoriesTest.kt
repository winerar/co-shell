package app.coshell.core.domain

import app.coshell.core.model.CategoryType
import app.coshell.core.model.SystemCategories
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SystemCategoriesTest {
    private val categories = FakeCategoryRepository()
    private val ensure = EnsureAdjustmentCategoriesUseCase(categories)
    private val expenseId = SystemCategories.ADJUSTMENT_EXPENSE_ID
    private val incomeId = SystemCategories.ADJUSTMENT_INCOME_ID

    @Test
    fun `creates both adjustment categories with fixed ids`() = runTest {
        val ids = ensure()

        assertEquals(mapOf(CategoryType.EXPENSE to expenseId, CategoryType.INCOME to incomeId), ids)
        assertEquals(CategoryType.EXPENSE, categories.getCategory(expenseId)?.type)
        assertEquals(CategoryType.INCOME, categories.getCategory(incomeId)?.type)
        assertEquals(SystemCategories.ADJUSTMENT_NAME, categories.getCategory(incomeId)?.name)
    }

    @Test
    fun `restores an archived one and keeps the user's name`() = runTest {
        ensure()
        val renamed = categories.getCategory(expenseId)!!.copy(name = "Выравнивание", archived = true)
        categories.saveCategory(renamed)

        ensure()

        assertEquals(renamed.copy(archived = false), categories.getCategory(expenseId))
    }

    @Test
    fun `recreates a deleted one and leaves live ones untouched`() = runTest {
        ensure()
        val income = categories.getCategory(incomeId)!!.copy(name = "Своё", color = 1)
        categories.saveCategory(income)
        categories.deleteCategory(expenseId)

        ensure()

        assertEquals(SystemCategories.adjustment(CategoryType.EXPENSE), categories.getCategory(expenseId))
        assertEquals(income, categories.getCategory(incomeId))
    }

    @Test
    fun `type of a system category cannot change`() = runTest {
        ensure()
        val save = SaveCategoryUseCase(categories, FakeTransactionRepository())
        val flipped = categories.getCategory(expenseId)!!.copy(type = CategoryType.INCOME)

        assertTrue(save.isTypeLocked(expenseId))
        val result = save(flipped)

        assertIs<SaveCategoryUseCase.Result.Invalid>(result)
        assertEquals(SaveCategoryUseCase.Reason.TYPE_LOCKED, result.reason)
        assertEquals(CategoryType.EXPENSE, categories.getCategory(expenseId)?.type)
    }

    @Test
    fun `only the fixed ids are adjustments`() {
        assertEquals(CategoryType.INCOME, SystemCategories.adjustmentType(incomeId))
        assertNull(SystemCategories.adjustmentType("food"))
        assertFalse(SystemCategories.isAdjustment(null))
        assertTrue(SystemCategories.isAdjustment(expenseId))
    }
}

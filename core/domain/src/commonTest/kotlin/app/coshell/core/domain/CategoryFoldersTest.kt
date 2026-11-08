package app.coshell.core.domain

import app.coshell.core.domain.SaveCategoryUseCase.Reason
import app.coshell.core.domain.SaveCategoryUseCase.Result
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType.EXPENSE
import app.coshell.core.model.CategoryType.INCOME
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class CategoryFoldersTest {
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()
    private val save = SaveCategoryUseCase(categories, transactions)

    private val car = Category("car", "Машина", EXPENSE, sortOrder = 0)
    private val fuel = Category("fuel", "Бензин", EXPENSE, parentId = "car", sortOrder = 0)
    private val food = Category("food", "Продукты", EXPENSE, sortOrder = 1)
    private val salary = Category("salary", "Зарплата", INCOME)

    @BeforeTest
    fun setUp() = runTest { categories.saveCategories(listOf(car, fuel, food, salary)) }

    @Test
    fun categoryCanBeMovedIntoFolderAndGoesToItsEnd() = runTest {
        val parking = Category("parking", "Парковка", EXPENSE)
        assertEquals(Result.Saved, save(parking.copy(parentId = "car")))
        assertEquals(1, categories.getCategory("parking")?.sortOrder)

        assertEquals(Result.Saved, save(food.copy(parentId = "car")))
        assertEquals("car", categories.getCategory("food")?.parentId)
        assertEquals(2, categories.getCategory("food")?.sortOrder)
    }

    @Test
    fun onlyOneLevelOfNesting() = runTest {
        assertEquals(Result.Invalid(Reason.PARENT_NOT_TOP_LEVEL), save(food.copy(parentId = "fuel")))
        assertEquals(Result.Invalid(Reason.HAS_SUBCATEGORIES), save(car.copy(parentId = "food")))
        assertEquals(Result.Invalid(Reason.PARENT_NOT_FOUND), save(food.copy(parentId = "food")))
        assertEquals(Result.Invalid(Reason.PARENT_NOT_FOUND), save(food.copy(parentId = "nope")))
    }

    @Test
    fun folderMustHaveSameType() = runTest {
        assertEquals(Result.Invalid(Reason.PARENT_TYPE_MISMATCH), save(food.copy(parentId = "salary")))
    }

    @Test
    fun typeIsLockedForUsedCategoriesAndFolders() = runTest {
        assertEquals(Result.Invalid(Reason.TYPE_LOCKED), save(car.copy(type = INCOME)))

        val ars = CurrencyCode("ARS")
        transactions.saveTransaction(
            Transaction("t", TransactionType.EXPENSE, "a", Money(1, ars), Instant.fromEpochSeconds(0), categoryId = "food"),
        )
        assertEquals(Result.Invalid(Reason.TYPE_LOCKED), save(food.copy(type = INCOME)))

        val gifts = Category("gifts", "Подарки", EXPENSE)
        categories.saveCategory(gifts)
        assertEquals(Result.Saved, save(gifts.copy(type = INCOME)))
    }

    @Test
    fun archivingFolderLiftsSubcategories() = runTest {
        ArchiveCategoryUseCase(categories)("car", archived = true)
        assertEquals(true, categories.getCategory("car")?.archived)
        assertNull(categories.getCategory("fuel")?.parentId)

        ArchiveCategoryUseCase(categories)("car", archived = false)
        assertEquals(false, categories.getCategory("car")?.archived)
    }

    @Test
    fun deletingFolderLiftsSubcategories() = runTest {
        assertEquals(DeleteCategoryUseCase.Result.DELETED, DeleteCategoryUseCase(categories, transactions)("car"))
        assertNull(categories.getCategory("car"))
        assertNull(categories.getCategory("fuel")?.parentId)
    }
}

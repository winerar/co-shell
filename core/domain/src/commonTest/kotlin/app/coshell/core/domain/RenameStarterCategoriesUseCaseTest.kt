package app.coshell.core.domain

import app.coshell.core.model.CategoryType
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.SystemCategories
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RenameStarterCategoriesUseCaseTest {
    private var counter = 0
    private val ids = IdGenerator { "id${counter++}" }
    private val spanish = CategoryNames { key ->
        when (key) {
            DefaultCategories.key(CategoryType.EXPENSE, "food") -> "Supermercado"
            DefaultCategories.key(CategoryType.EXPENSE, "taxi") -> "Taxi"
            CategoryNames.ADJUSTMENT -> "Ajuste de saldo"
            else -> null
        }
    }

    @Test
    fun `starter names follow the picked language`() = runTest {
        val categories = FakeCategoryRepository()
        SeedDefaultCategoriesUseCase(categories, ids, spanish)()
        EnsureAdjustmentCategoriesUseCase(categories, spanish)()

        RenameStarterCategoriesUseCase(categories)(from = spanish, to = CategoryNames.BuiltIn)

        val names = categories.items.value.values.map { it.name }
        assertEquals(true, "Продукты" in names)
        assertEquals(false, "Supermercado" in names)
        assertEquals("Корректировка баланса", categories.getCategory(SystemCategories.ADJUSTMENT_EXPENSE_ID)?.name)
    }

    @Test
    fun `names the user changed stay`() = runTest {
        val categories = FakeCategoryRepository()
        SeedDefaultCategoriesUseCase(categories, ids, spanish)()
        val food = categories.items.value.values.first { it.icon == "food" && it.type == CategoryType.EXPENSE }
        categories.saveCategory(food.copy(name = "Mercado del barrio"))

        RenameStarterCategoriesUseCase(categories)(from = spanish, to = CategoryNames.BuiltIn)

        assertEquals("Mercado del barrio", categories.getCategory(food.id)?.name)
    }

    @Test
    fun `the same language changes nothing`() = runTest {
        val categories = FakeCategoryRepository()
        SeedDefaultCategoriesUseCase(categories, ids, spanish)()
        assertEquals(0, RenameStarterCategoriesUseCase(categories)(from = spanish, to = spanish))
    }
}

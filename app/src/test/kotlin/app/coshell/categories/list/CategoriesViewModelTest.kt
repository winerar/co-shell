package app.coshell.categories.list

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType.EXPENSE
import app.coshell.core.model.CategoryType.INCOME
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CategoriesViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val categories = FakeCategoryRepository()

    private suspend fun seed() = categories.saveCategories(
        listOf(
            Category("car", "Машина", EXPENSE, sortOrder = 0),
            Category("fuel", "Бензин", EXPENSE, parentId = "car", sortOrder = 0),
            Category("taxi", "Такси", EXPENSE, sortOrder = 1),
            Category("food", "Продукты", EXPENSE, sortOrder = 2),
            Category("old", "Старое", EXPENSE, archived = true),
            Category("salary", "Зарплата", INCOME),
        ),
    )

    @Test
    fun `list groups subcategories under folders`() = runTest {
        seed()
        val vm = CategoriesViewModel(categories)
        vm.state.launchIn(backgroundScope)
        vm.toggleFolder("car")

        val state = vm.state.first { !it.isLoading }
        assertEquals(listOf("car", "taxi", "food"), state.groups.map { it.item.id })
        val car = state.groups.first()
        assertTrue(car.isFolder && car.expanded)
        assertEquals(listOf("fuel"), car.children.map { it.id })
        assertEquals(listOf("old"), state.archived.map { it.id })

        vm.selectType(INCOME)
        assertEquals(listOf("salary"), vm.state.first { it.type == INCOME }.groups.map { it.item.id })
    }
}

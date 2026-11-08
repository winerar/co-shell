package app.coshell.categories.edit

import androidx.lifecycle.SavedStateHandle
import app.coshell.core.domain.ArchiveCategoryUseCase
import app.coshell.core.domain.DeleteCategoryUseCase
import app.coshell.core.domain.SaveCategoryUseCase
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType.EXPENSE
import app.coshell.core.model.CategoryType.INCOME
import app.coshell.core.model.IdGenerator
import app.coshell.navigation.CategoryEditDestination as Args
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CategoryEditViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()

    private suspend fun seed() = categories.saveCategories(
        listOf(
            Category("car", "Машина", EXPENSE, sortOrder = 0),
            Category("fuel", "Бензин", EXPENSE, parentId = "car", sortOrder = 0),
            Category("taxi", "Такси", EXPENSE, icon = "taxi", color = 0xFFFFC107, sortOrder = 1),
            Category("food", "Продукты", EXPENSE, sortOrder = 2),
            Category("old", "Старое", EXPENSE, archived = true),
            Category("salary", "Зарплата", INCOME),
        ),
    )

    private fun editor(vararg args: Pair<String, Any?>) = CategoryEditViewModel(
        savedStateHandle = SavedStateHandle(mapOf(*args)),
        categories = categories,
        saveCategory = SaveCategoryUseCase(categories, transactions),
        archiveCategory = ArchiveCategoryUseCase(categories),
        deleteCategory = DeleteCategoryUseCase(categories, transactions),
        ids = IdGenerator { "new" },
    )

    @Test
    fun `new folder collects chosen categories`() = runTest {
        seed()
        val vm = editor(Args.ARG_TYPE to "EXPENSE", Args.ARG_AS_FOLDER to true)
        val state = vm.state.value
        assertTrue(state.asFolder)
        // "car" already has a subcategory, so it cannot go into another folder.
        assertEquals(listOf("taxi", "food"), state.candidates.map { it.id })

        vm.onNameChange("Транспорт")
        vm.onToggleChild("taxi")
        vm.save()

        assertTrue(vm.state.value.isDone)
        assertEquals("Транспорт", categories.getCategory("new")?.name)
        assertEquals("new", categories.getCategory("taxi")?.parentId)
        assertNull(categories.getCategory("food")?.parentId)
    }

    @Test
    fun `new category can be created inside a folder`() = runTest {
        seed()
        val vm = editor(Args.ARG_TYPE to "EXPENSE", Args.ARG_PARENT_ID to "car")
        assertEquals("car", vm.state.value.parentId)
        assertTrue(vm.state.value.candidates.isEmpty())

        vm.onNameChange("Парковка")
        vm.save()
        assertEquals("car", categories.getCategory("new")?.parentId)
    }

    @Test
    fun `editing a folder can take subcategories out`() = runTest {
        seed()
        val vm = editor(Args.ARG_CATEGORY_ID to "car")
        assertEquals(setOf("fuel"), vm.state.value.childIds)
        assertFalse(vm.state.value.canMoveToFolder)

        vm.onToggleChild("fuel")
        assertTrue(vm.state.value.canMoveToFolder)
        vm.save()
        assertNull(categories.getCategory("fuel")?.parentId)
    }

    @Test
    fun `empty name is rejected`() = runTest {
        seed()
        val vm = editor(Args.ARG_TYPE to "INCOME")
        vm.save()
        assertTrue(vm.state.value.nameError)
        assertEquals(INCOME, vm.state.value.type)
    }

    @Test
    fun `archiving a folder lifts its subcategories`() = runTest {
        seed()
        val vm = editor(Args.ARG_CATEGORY_ID to "car")
        vm.toggleArchived()
        assertTrue(vm.state.value.isDone)
        assertEquals(true, categories.getCategory("car")?.archived)
        assertNull(categories.getCategory("fuel")?.parentId)
    }

    @Test
    fun `icon and colour are loaded and saved`() = runTest {
        seed()
        val vm = editor(Args.ARG_CATEGORY_ID to "taxi")
        assertEquals("taxi", vm.state.value.icon)
        assertEquals(0xFFFFC107, vm.state.value.color)

        vm.onIconChange("car")
        vm.onColorChange(0xFF2196F3)
        vm.save()

        val saved = categories.getCategory("taxi")
        assertEquals("car", saved?.icon)
        assertEquals(0xFF2196F3, saved?.color)
    }
}

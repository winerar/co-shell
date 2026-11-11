package app.coshell.templates.edit

import androidx.lifecycle.SavedStateHandle
import app.coshell.core.domain.templates.SaveTemplateUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import app.coshell.navigation.TemplateEditDestination
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeTemplateRepository
import app.coshell.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TemplateEditViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val templates = FakeTemplateRepository()

    private suspend fun seed() {
        accounts.saveAccount(Account("card", "Galicia", ars, sortOrder = 0))
        accounts.saveAccount(Account("cash", "Наличные", usd, sortOrder = 1))
        accounts.saveAccount(Account("old", "Старый", ars, archived = true))
        categories.saveCategory(Category("edu", "Образование", CategoryType.EXPENSE))
        categories.saveCategory(Category("lang", "Языки", CategoryType.EXPENSE, parentId = "edu"))
        categories.saveCategory(Category("salary", "Зарплата", CategoryType.INCOME))
    }

    private fun viewModel(templateId: String? = null) = TemplateEditViewModel(
        savedStateHandle = SavedStateHandle(mapOf(TemplateEditDestination.ARG_TEMPLATE_ID to templateId)),
        accounts = accounts,
        categories = categories,
        templates = templates,
        saveTemplate = SaveTemplateUseCase(accounts, categories, templates),
        ids = IdGenerator { "new" },
        moneyFormat = MoneyFormat.RU,
    )

    @Test
    fun `a new template offers live accounts and categories of its type`() = runTest {
        seed()
        val state = viewModel().state.value
        assertTrue(state.isNew)
        assertEquals(listOf("card", "cash"), state.accounts.map { it.id })
        assertEquals("card", state.accountId)
        assertEquals(listOf("Образование", "Образование / Языки"), state.categoryChoices.map { it.label })
    }

    @Test
    fun `saves a template without an amount at the end of the list`() = runTest {
        seed()
        templates.saveTemplate(TransactionTemplate("a", "Аренда", TransactionType.EXPENSE, "cash", usd, sortOrder = 4))
        val vm = viewModel()
        vm.onNameChange(" Испанский ")
        vm.onCategorySelect("lang")
        vm.onNoteChange("Марта")
        vm.save()

        val saved = templates.getTemplate("new")!!
        assertEquals("Испанский", saved.name)
        assertNull(saved.amount)
        assertEquals(ars, saved.currency)
        assertEquals("lang", saved.categoryId)
        assertEquals(5, saved.sortOrder)
        assertTrue(vm.state.value.isDone)
    }

    @Test
    fun `the amount is parsed in the currency of the chosen account`() = runTest {
        seed()
        val vm = viewModel()
        vm.onNameChange("Аренда")
        vm.onAccountSelect("cash")
        vm.onAmountChange("650")
        vm.save()
        assertEquals(Money(650_00, usd), templates.getTemplate("new")!!.amount)
    }

    @Test
    fun `name, a bad amount and a missing transfer target are reported`() = runTest {
        seed()
        val vm = viewModel()
        vm.onTypeChange(TransactionType.TRANSFER)
        vm.onAmountChange("abc")
        vm.save()

        val state = vm.state.value
        assertTrue(state.nameError)
        assertTrue(state.amountError)
        assertTrue(state.toAccountError)
        assertFalse(state.isDone)
        assertTrue(templates.items.value.isEmpty())
    }

    @Test
    fun `a transfer template keeps the target and drops the category`() = runTest {
        seed()
        val vm = viewModel()
        vm.onNameChange("В доллары")
        vm.onCategorySelect("edu")
        vm.onTypeChange(TransactionType.TRANSFER)
        vm.onToAccountSelect("cash")
        vm.save()

        val saved = templates.getTemplate("new")!!
        assertEquals("cash", saved.toAccountId)
        assertNull(saved.categoryId)
    }

    @Test
    fun `switching the type drops a category of the other type`() = runTest {
        seed()
        val vm = viewModel()
        vm.onCategorySelect("edu")
        vm.onTypeChange(TransactionType.INCOME)
        assertNull(vm.state.value.categoryId)
        assertEquals(listOf("salary"), vm.state.value.categoryChoices.map { it.id })
    }

    @Test
    fun `editing keeps the id and order, an archived account must be chosen again`() = runTest {
        seed()
        templates.saveTemplate(
            TransactionTemplate("t", "Старое", TransactionType.EXPENSE, "old", ars, Money(100_00, ars), "edu", sortOrder = 3),
        )
        val vm = viewModel("t")
        assertFalse(vm.state.value.isNew)
        assertNull(vm.state.value.accountId)
        assertEquals("", vm.state.value.amount)

        vm.save()
        assertTrue(vm.state.value.accountError)

        vm.onAccountSelect("card")
        vm.onAmountChange("120")
        vm.save()
        val saved = templates.getTemplate("t")!!
        assertEquals("card", saved.accountId)
        assertEquals(Money(120_00, ars), saved.amount)
        assertEquals(3, saved.sortOrder)
    }

    @Test
    fun `delete removes the template`() = runTest {
        seed()
        templates.saveTemplate(TransactionTemplate("t", "Аренда", TransactionType.EXPENSE, "cash", usd))
        val vm = viewModel("t")
        vm.delete()
        assertTrue(templates.items.value.isEmpty())
        assertTrue(vm.state.value.isDone)
    }
}

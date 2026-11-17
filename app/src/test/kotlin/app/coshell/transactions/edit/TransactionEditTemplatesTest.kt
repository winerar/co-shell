package app.coshell.transactions.edit

import app.coshell.transactions.edit.TransactionEditUiState.EntryStep
import app.coshell.testing.FakeTemplateUsage
import app.coshell.testing.fakeEntryTags
import app.coshell.testing.fakeEntryTemplates
import app.coshell.testing.FakeTemplateRepository
import app.coshell.core.model.TransactionTemplate
import androidx.lifecycle.SavedStateHandle
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.navigation.TransactionEditDestination
import app.coshell.transactions.DeleteTransactionWithUndo
import app.coshell.undo.UndoCenter
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
import app.coshell.core.designsystem.components.keypad.KeypadKey
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test


/** Templates in the entry sheet: the strip, usage, «Сохранить как шаблон». */
class TransactionEditTemplatesTest : TransactionEditTestBase() {
    @Test
    fun `a template chip fills the sheet and opens it on the amount`() = runTest {
        seedTemplate()
        val vm = viewModel()
        assertEquals(EntryStep.ACCOUNT, vm.state.value.step)

        vm.onTemplatePick("spanish")

        val state = vm.state.value
        assertEquals(EntryStep.DETAILS, state.step)
        assertEquals("Испанский", state.templateName)
        assertEquals("card", state.accountId)
        assertEquals(MoneyFormat.RU.formatForEditing(Money(40_000_00, ars)), state.amount)
        assertEquals("cafe", state.categoryId)
        assertEquals("Марта", state.note)
        assertTrue(state.isToday)
        assertTrue(state.isNew)
    }

    @Test
    fun `saving an operation from a template counts its usage`() = runTest {
        seedTemplate()
        val vm = viewModel(templateId = "spanish")
        assertEquals("Испанский", vm.state.value.templateName)

        vm.save()

        val saved = transactions.getTransaction("new")!!
        assertEquals(Money(40_000_00, ars), saved.amount)
        assertEquals("cafe", saved.categoryId)
        assertEquals(mapOf("spanish" to 1), usage.state.value)
    }

    @Test
    fun `a template without an amount asks for it`() = runTest {
        seedTemplate(amount = null)
        val vm = viewModel(templateId = "spanish")
        assertEquals("", vm.state.value.amount)

        vm.save()

        assertEquals(TransactionEditUiState.AmountError.REQUIRED, vm.state.value.amountError)
        assertTrue(usage.state.value.isEmpty())
    }

    @Test
    fun `a template whose account or category is archived asks to choose them`() = runTest {
        seedTemplate()
        accounts.saveAccount(Account("card", "Карта", ars, archived = true))
        categories.saveCategory(Category("cafe", "Кафе", CategoryType.EXPENSE, parentId = "food", archived = true))

        val state = viewModel(templateId = "spanish").state.value

        assertEquals(EntryStep.ACCOUNT, state.step)
        assertNull(state.accountId)
        assertEquals("", state.amount) // the amount was in the gone account's currency
        assertNull(state.categoryId)
        assertTrue(state.categoryError)
    }

    @Test
    fun `clearing the template returns to an empty new operation`() = runTest {
        seedTemplate()
        val vm = viewModel(templateId = "spanish")

        vm.onTemplateClear()

        val state = vm.state.value
        assertNull(state.templateName)
        assertEquals(EntryStep.ACCOUNT, state.step)
        assertEquals("", state.amount)
        vm.onAccountSelect("card")
        vm.onAmountChange("10")
        vm.onCategorySelect("food")
        vm.save()
        assertTrue(usage.state.value.isEmpty())
    }

    @Test
    fun `save as template keeps the amount only when asked`() = runTest {
        seed()
        transactions.saveTransaction(
            Transaction("t", TransactionType.EXPENSE, "card", Money(32_000_00, ars), now, categoryId = "food", note = "Coto"),
        )
        val vm = viewModel("t")

        vm.saveAsTemplate(" Продукты Coto ", rememberAmount = false)

        val template = templates.getTemplate("tpl-new")!!
        assertEquals("Продукты Coto", template.name)
        assertNull(template.amount)
        assertEquals("card", template.accountId)
        assertEquals("food", template.categoryId)
        assertEquals("Coto", template.note)
        assertEquals("Продукты Coto", vm.state.value.savedTemplateName)
        assertFalse(vm.state.value.templateSaveFailed)
    }

    @Test
    fun `save as template is refused for an archived account`() = runTest {
        seed()
        transactions.saveTransaction(Transaction("t", TransactionType.EXPENSE, "old", Money(5_00, ars), now, categoryId = "food"))
        val vm = viewModel("t")

        vm.saveAsTemplate("Старое", rememberAmount = true)

        assertTrue(templates.items.value.isEmpty())
        assertTrue(vm.state.value.templateSaveFailed)
        assertNull(vm.state.value.savedTemplateName)
    }

    // endregion
}

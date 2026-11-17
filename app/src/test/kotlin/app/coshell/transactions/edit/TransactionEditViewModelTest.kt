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


class TransactionEditViewModelTest : TransactionEditTestBase() {
    @Test
    fun `new expense defaults to today and the first active account`() = runTest {
        seed()
        val state = viewModel().state.value
        assertEquals(LocalDate(2026, 11, 8), state.date)
        assertTrue(state.isToday)
        assertEquals("card", state.accountId)
        assertEquals(listOf("card", "cash"), state.accounts.map { it.id })
        assertEquals(listOf("food"), state.categories.map { it.item.id })
        assertTrue(state.categories.single().isFolder)
    }

    @Test
    fun `the editor reads only the newest operations, not the whole history`() = runTest {
        seed()
        viewModel().state.value
        assertEquals(listOf(500), transactions.queries.map { it.limit })
    }

    @Test
    fun `expense is saved with the chosen category and date`() = runTest {
        seed()
        val vm = viewModel()
        vm.onAmountChange("1 500")
        vm.onCategorySelect("cafe")
        assertEquals("food", vm.state.value.openFolderId)
        vm.onDateSelect(LocalDate(2026, 11, 7))
        vm.onNoteChange("кофе")
        vm.save()

        assertTrue(vm.state.value.isDone)
        val saved = transactions.getTransaction("new")!!
        assertEquals(TransactionType.EXPENSE, saved.type)
        assertEquals(Money(1_500_00, ars), saved.amount)
        assertEquals("cafe", saved.categoryId)
        assertEquals(LocalDateTime(2026, 11, 7, 14, 30).toInstant(zone), saved.date)
        assertEquals("кофе", saved.note)
    }

    @Test
    fun `switching type drops a category of the other type`() = runTest {
        seed()
        val vm = viewModel()
        vm.onCategorySelect("food")
        vm.onTypeChange(TransactionType.INCOME)
        assertNull(vm.state.value.categoryId)
        assertEquals(listOf("salary"), vm.state.value.categories.map { it.item.id })
    }

    @Test
    fun `invalid form shows errors and saves nothing`() = runTest {
        seed()
        val vm = viewModel()
        vm.save()
        assertEquals(TransactionEditUiState.AmountError.REQUIRED, vm.state.value.amountError)
        assertTrue(vm.state.value.categoryError)
        assertFalse(vm.state.value.isDone)
        assertTrue(transactions.items.value.isEmpty())
    }

    @Test
    fun `transfer between currencies saves both amounts`() = runTest {
        seed()
        val vm = viewModel(type = "TRANSFER")
        vm.onAccountSelect("cash")
        vm.onToAccountSelect("card")
        assertTrue(vm.state.value.needsToAmount)
        vm.onAmountChange("100")
        vm.onToAmountChange("120 000")
        vm.save()

        val saved = transactions.getTransaction("new")!!
        assertEquals(Money(100_00, usd), saved.amount)
        assertEquals("card", saved.toAccountId)
        assertEquals(Money(120_000_00, ars), saved.toAmount)
        assertNull(saved.categoryId)
    }

    @Test
    fun `new operation starts on the account and moves on after a tap`() = runTest {
        seed()
        val vm = viewModel()
        assertEquals(TransactionEditUiState.EntryStep.ACCOUNT, vm.state.value.step)
        vm.onAccountSelect("cash")
        assertEquals(TransactionEditUiState.EntryStep.DETAILS, vm.state.value.step)
        vm.onStepChange(TransactionEditUiState.EntryStep.ACCOUNT)
        vm.onAccountSelect("card")
        assertEquals(TransactionEditUiState.EntryStep.DETAILS, vm.state.value.step)
        assertEquals("card", vm.state.value.accountId)
    }

    @Test
    fun `transfer asks the target account before the amount`() = runTest {
        seed()
        val vm = viewModel(type = "TRANSFER")
        vm.onAccountSelect("cash")
        assertEquals(TransactionEditUiState.EntryStep.TO_ACCOUNT, vm.state.value.step)
        vm.onToAccountSelect("card")
        assertEquals(TransactionEditUiState.EntryStep.DETAILS, vm.state.value.step)
    }

    @Test
    fun `keypad adds up the amount and done saves`() = runTest {
        seed()
        val vm = viewModel()
        vm.onAccountSelect("card")
        listOf('3', '2').forEach { vm.onKey(KeypadKey.Digit(it)) }
        vm.onKey(KeypadKey.TripleZero)
        vm.onKey(KeypadKey.Plus)
        listOf('1', '5').forEach { vm.onKey(KeypadKey.Digit(it)) }
        vm.onKey(KeypadKey.TripleZero)
        assertEquals("32000 + 15000", vm.state.value.amount)
        vm.onCategorySelect("food")
        vm.onKey(KeypadKey.Done)
        assertEquals(Money(47_000_00, ars), transactions.getTransaction("new")!!.amount)
    }

    @Test
    fun `edited operation opens on the details`() = runTest {
        seed()
        val vm = viewModel()
        vm.onAmountChange("10")
        vm.onCategorySelect("food")
        vm.save()
        assertEquals(TransactionEditUiState.EntryStep.DETAILS, viewModel(transactionId = "new").state.value.step)
    }

    @Test
    fun `duplicate is a new operation today with the same fields`() = runTest {
        seed()
        val first = viewModel()
        first.onAmountChange("250")
        first.onCategorySelect("cafe")
        first.onDateSelect(LocalDate(2026, 11, 1))
        first.save()
        val copy = viewModel(copyFrom = "new").state.value
        assertTrue(copy.isNew)
        assertEquals(TransactionEditUiState.EntryStep.DETAILS, copy.step)
        assertEquals(LocalDate(2026, 11, 8), copy.date)
        assertEquals("cafe", copy.categoryId)
        assertEquals("card", copy.accountId)
    }

    @Test
    fun `date picker millis map to the local date`() = runTest {
        seed()
        val vm = viewModel()
        val millis = LocalDateTime(2026, 10, 31, 0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()
        vm.onDatePicked(millis)
        assertEquals(LocalDate(2026, 10, 31), vm.state.value.date)
        assertEquals(millis, vm.state.value.dateMillisUtc)
    }

    @Test
    fun `existing transaction is edited in place and keeps its time and archived account`() = runTest {
        seed()
        val date = LocalDateTime(2026, 11, 1, 9, 15).toInstant(zone)
        transactions.saveTransaction(
            Transaction("t", TransactionType.EXPENSE, "old", Money(250_00, ars), date, categoryId = "food", payee = "Coto"),
        )
        val vm = viewModel("t")
        val state = vm.state.value
        assertFalse(state.isNew)
        assertEquals("250", state.amount)
        assertEquals("old", state.accountId)
        assertTrue(state.accounts.any { it.id == "old" })

        vm.onAmountChange("300")
        vm.onDateSelect(LocalDate(2026, 11, 2))
        vm.save()
        val saved = transactions.getTransaction("t")!!
        assertEquals(Money(300_00, ars), saved.amount)
        assertEquals(LocalDateTime(2026, 11, 2, 9, 15).toInstant(zone), saved.date)
        assertEquals("Coto", saved.payee)
    }

    @Test
    fun `delete removes the transaction`() = runTest {
        seed()
        transactions.saveTransaction(Transaction("t", TransactionType.EXPENSE, "card", Money(1, ars), now))
        val vm = viewModel("t")
        vm.delete()
        assertTrue(vm.state.value.isDone)
        assertNull(transactions.getTransaction("t"))
    }
}

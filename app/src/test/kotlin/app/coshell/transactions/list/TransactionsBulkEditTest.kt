package app.coshell.transactions.list

import app.coshell.R
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Tag
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeReportCurrencySettings
import app.coshell.testing.FakeTagRepository
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
import app.coshell.transactions.BulkEditWithUndo
import app.coshell.transactions.DeleteTransactionWithUndo
import app.coshell.transactions.list.bulk.BulkEditState
import app.coshell.undo.UndoCenter
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** «Изменить» in the selection mode (#794): which sheets open, what applying does, how undo brings the rows back. */
class TransactionsBulkEditTest {
    @get:Rule val main = MainDispatcherRule()

    private val ars = CurrencyCode("ARS")
    private val now = Instant.fromEpochSeconds(1_794_000_000)
    private val transactions = FakeTransactionRepository()
    private val categories = FakeCategoryRepository()
    private val tags = FakeTagRepository(Tag("trip", "Отпуск"))
    private val undo = UndoCenter()

    private suspend fun TestScope.vm(): TransactionsViewModel {
        categories.saveCategory(Category("food", "Продукты", CategoryType.EXPENSE))
        categories.saveCategory(Category("cafe", "Кафе", CategoryType.EXPENSE))
        categories.saveCategory(Category("salary", "Зарплата", CategoryType.INCOME))
        categories.saveCategory(Category("old", "Старая", CategoryType.EXPENSE, archived = true))
        transactions.saveTransaction(Transaction("e1", TransactionType.EXPENSE, "card", Money(100, ars), now))
        transactions.saveTransaction(Transaction("e2", TransactionType.EXPENSE, "card", Money(200, ars), now, categoryId = "cafe"))
        transactions.saveTransaction(Transaction("i1", TransactionType.INCOME, "card", Money(300, ars), now))
        return TransactionsViewModel(
            transactions, FakeAccountRepository().apply { saveAccount(Account("card", "Карта", ars)) }, categories, { flowOf(emptyMap()) },
            MoneyFormat.RU, object : Clock { override fun now() = now }, TimeZone.UTC,
            DeleteTransactionWithUndo(transactions, undo), BulkEditWithUndo(transactions, categories, undo),
            FakeExchangeRateRepository(), FakeReportCurrencySettings(), tags,
        ).also {
            it.state.launchIn(backgroundScope)
            it.state.first { s -> !s.isLoading }
        }
    }

    @Test
    fun `expenses offer live expense categories and the tags`() = runTest {
        val vm = vm()
        vm.onLongPress("e1")
        vm.onToggle("e2")
        vm.openBulkEdit()
        val sheet = vm.bulk.first { it != null }!!
        assertEquals(BulkEditState.Step.MENU, sheet.step)
        assertEquals(2, sheet.count)
        assertTrue(sheet.categoryAllowed)
        assertEquals(setOf("food", "cafe"), sheet.categories.map { it.id }.toSet())
        assertEquals(listOf("trip"), sheet.tags.map { it.id })
    }

    @Test
    fun `an income among expenses turns the category off`() = runTest {
        val vm = vm()
        vm.onLongPress("e1")
        vm.onToggle("i1")
        vm.openBulkEdit()
        val sheet = vm.bulk.first { it != null }!!
        assertFalse(sheet.categoryAllowed)
        assertEquals(emptyList<String>(), sheet.categories.map { it.id })
    }

    @Test
    fun `steps move between the menu and a picker and closing drops the sheet`() = runTest {
        val vm = vm()
        vm.onLongPress("e1")
        vm.openBulkEdit()
        vm.bulk.first { it != null }
        vm.bulkStep(BulkEditState.Step.TAGS)
        assertEquals(BulkEditState.Step.TAGS, vm.bulk.value?.step)
        vm.closeBulkEdit()
        assertEquals(null, vm.bulk.value)
        assertTrue(vm.selection.value.active) // closing the sheet keeps the rows chosen
    }

    @Test
    fun `applying a category changes the rows ends the mode and undo brings them back`() = runTest {
        val vm = vm()
        vm.onLongPress("e1")
        vm.onToggle("e2")
        vm.openBulkEdit()
        vm.bulk.first { it != null }
        vm.applyBulkCategory("food")
        assertEquals(null, vm.bulk.value)
        assertFalse(vm.selection.value.active)
        assertEquals("food", transactions.getTransaction("e1")?.categoryId)
        assertEquals("food", transactions.getTransaction("e2")?.categoryId)
        val offer = checkNotNull(undo.latest)
        assertEquals(R.string.transactions_changed_many, offer.message)
        assertEquals(listOf<Any>(2), offer.args)
        offer.undo()
        assertEquals(null, transactions.getTransaction("e1")?.categoryId)
        assertEquals("cafe", transactions.getTransaction("e2")?.categoryId)
    }

    @Test
    fun `applying tags adds them to every chosen row`() = runTest {
        val vm = vm()
        vm.onLongPress("e1")
        vm.onToggle("i1")
        vm.openBulkEdit()
        vm.bulk.first { it != null }
        vm.applyBulkTags(listOf("trip"))
        assertEquals(listOf("trip"), transactions.getTransaction("e1")?.labels)
        assertEquals(listOf("trip"), transactions.getTransaction("i1")?.labels)
        assertEquals(emptyList<String>(), transactions.getTransaction("e2")?.labels)
    }
}

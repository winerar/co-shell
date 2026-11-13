package app.coshell.transactions.edit

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
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeTemplateRepository
import app.coshell.testing.FakeTemplateUsage
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
import app.coshell.testing.fakeEntryTags
import app.coshell.testing.fakeEntryTemplates
import app.coshell.transactions.DeleteTransactionWithUndo
import app.coshell.undo.UndoCenter
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** «↩ Возврат» in the entry sheet (#208, board Fb208C): an income saved into an expense category. */
class TransactionEditRefundTest {
    @get:Rule val main = MainDispatcherRule()

    private val zone = TimeZone.of("America/Argentina/Buenos_Aires")
    private val now = LocalDateTime(2026, 11, 8, 14, 30).toInstant(zone)
    private val ars = CurrencyCode("ARS")

    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()

    private suspend fun seed() {
        accounts.saveAccount(Account("card", "Карта", ars, sortOrder = 0))
        categories.saveCategory(Category("cafe", "Кафе", CategoryType.EXPENSE))
        categories.saveCategory(Category("salary", "Зарплата", CategoryType.INCOME))
    }

    private fun viewModel(transactionId: String? = null) = TransactionEditViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(TransactionEditDestination.ARG_TRANSACTION_ID to transactionId, TransactionEditDestination.ARG_TYPE to "EXPENSE"),
        ),
        accounts = accounts,
        categories = categories,
        transactions = transactions,
        saveTransaction = SaveTransactionUseCase(accounts, categories, transactions),
        ids = IdGenerator { "new" },
        moneyFormat = MoneyFormat.RU,
        clock = object : Clock { override fun now(): Instant = now },
        timeZone = zone,
        rates = FakeExchangeRateRepository(),
        deleteWithUndo = DeleteTransactionWithUndo(transactions, UndoCenter()),
        entryTemplates = fakeEntryTemplates(accounts, categories, FakeTemplateRepository(), FakeTemplateUsage()),
        entryTags = fakeEntryTags(),
    )

    @Test
    fun `refund is saved as an income in the expense category`() = runTest {
        seed()
        val vm = viewModel()
        vm.onRefundToggle(true)
        vm.onAmountChange("4 500")
        vm.onCategorySelect("cafe")
        assertEquals("Кафе", vm.state.value.categoryName)
        vm.save()

        val saved = transactions.getTransaction("new")!!
        assertEquals(TransactionType.INCOME, saved.type)
        assertEquals("cafe", saved.categoryId)
        assertEquals(Money(4_500_00, ars), saved.amount)
    }

    @Test
    fun `a saved refund opens as an expense with the chip on`() = runTest {
        seed()
        transactions.saveTransaction(Transaction("r", TransactionType.INCOME, "card", Money(4_500_00, ars), now, categoryId = "cafe"))
        val state = viewModel("r").state.value
        assertEquals(TransactionType.EXPENSE, state.type)
        assertTrue(state.refund)
        assertEquals(listOf("cafe"), state.categories.map { it.item.id })
    }

    @Test
    fun `a plain income stays an income`() = runTest {
        seed()
        transactions.saveTransaction(Transaction("i", TransactionType.INCOME, "card", Money(1_00, ars), now, categoryId = "salary"))
        val state = viewModel("i").state.value
        assertEquals(TransactionType.INCOME, state.type)
        assertFalse(state.refund)
    }

    @Test
    fun `switching type turns the refund off`() = runTest {
        seed()
        val vm = viewModel()
        vm.onRefundToggle(true)
        vm.onTypeChange(TransactionType.INCOME)
        assertFalse(vm.state.value.refund)
        vm.onRefundToggle(true) // only an expense can be a refund
        assertFalse(vm.state.value.refund)
    }
}

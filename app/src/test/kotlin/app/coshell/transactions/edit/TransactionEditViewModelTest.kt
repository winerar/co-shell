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
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
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

class TransactionEditViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val zone = TimeZone.of("America/Argentina/Buenos_Aires")
    private val now = LocalDateTime(2026, 11, 8, 14, 30).toInstant(zone)
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")

    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()

    private suspend fun seed() {
        accounts.saveAccount(Account("card", "Карта", ars, sortOrder = 0))
        accounts.saveAccount(Account("cash", "Наличные", usd, sortOrder = 1))
        accounts.saveAccount(Account("old", "Старый", ars, archived = true))
        categories.saveCategory(Category("food", "Еда", CategoryType.EXPENSE))
        categories.saveCategory(Category("cafe", "Кафе", CategoryType.EXPENSE, parentId = "food"))
        categories.saveCategory(Category("salary", "Зарплата", CategoryType.INCOME))
    }

    private fun viewModel(transactionId: String? = null, type: String = "EXPENSE") = TransactionEditViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(TransactionEditDestination.ARG_TRANSACTION_ID to transactionId, TransactionEditDestination.ARG_TYPE to type),
        ),
        accounts = accounts,
        categories = categories,
        transactions = transactions,
        saveTransaction = SaveTransactionUseCase(accounts, categories, transactions),
        ids = IdGenerator { "new" },
        moneyFormat = MoneyFormat.RU,
        clock = object : Clock { override fun now(): Instant = now },
        timeZone = zone,
    )

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

    @Test
    fun `purchase in pesos paid from a dollar account keeps both amounts`() = runTest {
        seed()
        val vm = viewModel()
        vm.onAccountSelect("cash")
        vm.onReceiptToggle(true)
        assertEquals("ARS", vm.state.value.receiptCurrency) // the only other currency among accounts
        vm.onAmountChange("14,29")
        vm.onReceiptAmountChange("15 000")
        assertEquals("1 USD = 1\u00A0049,69 ARS", receiptRateText(vm.state.value))
        vm.onCategorySelect("food")
        vm.save()

        val saved = transactions.getTransaction("new")!!
        assertEquals(Money(14_29, usd), saved.amount)
        assertEquals(Money(15_000_00, ars), saved.originalAmount)
    }

    @Test
    fun `receipt in the account currency is refused and the receipt is reloaded on edit`() = runTest {
        seed()
        val vm = viewModel()
        vm.onReceiptToggle(true)
        vm.onReceiptCurrencyChange("ars") // account "card" is in ARS
        vm.onAmountChange("100")
        vm.onReceiptAmountChange("1")
        vm.onCategorySelect("food")
        vm.save()
        assertEquals(TransactionEditUiState.ReceiptCurrencyError.SAME_AS_ACCOUNT, vm.state.value.receiptCurrencyError)
        assertTrue(transactions.items.value.isEmpty())

        transactions.saveTransaction(
            Transaction(
                "t", TransactionType.EXPENSE, "cash", Money(10_00, usd), now,
                categoryId = "food", originalAmount = Money(10_500_00, ars),
            ),
        )
        val edit = viewModel("t").state.value
        assertTrue(edit.hasReceipt)
        assertEquals("ARS", edit.receiptCurrency)
        assertEquals(MoneyFormat.RU.formatForEditing(Money(10_500_00, ars)), edit.receiptAmount)
        assertEquals(listOf("ARS", "USD"), edit.receiptCurrencies) // earlier receipts first
    }

    @Test
    fun `switching to transfer drops the receipt`() = runTest {
        seed()
        val vm = viewModel()
        vm.onAccountSelect("cash")
        vm.onReceiptToggle(true)
        vm.onReceiptAmountChange("15 000")
        vm.onTypeChange(TransactionType.TRANSFER)
        vm.onToAccountSelect("card")
        vm.onAmountChange("10")
        vm.onToAmountChange("10 500")
        vm.save()
        assertNull(transactions.getTransaction("new")!!.originalAmount)
    }
}

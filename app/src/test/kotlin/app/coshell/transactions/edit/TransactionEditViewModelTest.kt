package app.coshell.transactions.edit

import app.coshell.transactions.edit.TransactionEditUiState.EntryStep
import app.coshell.testing.FakeTemplateUsage
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

    private val templates = FakeTemplateRepository()
    private val usage = FakeTemplateUsage()

    private fun viewModel(
        transactionId: String? = null,
        type: String = "EXPENSE",
        copyFrom: String? = null,
        templateId: String? = null,
    ) = TransactionEditViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(
                TransactionEditDestination.ARG_TRANSACTION_ID to transactionId,
                TransactionEditDestination.ARG_TYPE to type,
                TransactionEditDestination.ARG_COPY_FROM to copyFrom,
                TransactionEditDestination.ARG_TEMPLATE_ID to templateId,
            ),
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
        entryTemplates = fakeEntryTemplates(accounts, categories, templates, usage),
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

    // region Templates (#104)

    private suspend fun seedTemplate(amount: Money? = Money(40_000_00, ars), categoryId: String? = "cafe") {
        seed()
        templates.saveTemplate(
            TransactionTemplate("spanish", "Испанский", TransactionType.EXPENSE, "card", ars, amount, categoryId, note = "Марта"),
        )
    }

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

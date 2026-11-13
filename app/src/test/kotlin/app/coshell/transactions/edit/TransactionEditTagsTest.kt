package app.coshell.transactions.edit

import androidx.lifecycle.SavedStateHandle
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.tags.TagUsage
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Tag
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.navigation.TransactionEditDestination
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeTagRepository
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
import org.junit.Rule
import org.junit.Test

/** Tags in the entry sheet (#293): the pill row, choosing, creating and saving into labels. */
class TransactionEditTagsTest {
    @get:Rule val main = MainDispatcherRule()

    private val zone = TimeZone.of("America/Argentina/Buenos_Aires")
    private val now = LocalDateTime(2026, 11, 13, 12, 0).toInstant(zone)
    private val ars = CurrencyCode("ARS")

    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()
    private val tags = FakeTagRepository(
        Tag("аня", "Аня", 0xFFFF77AAL),
        Tag("mercado libre", "Mercado Libre"),
        Tag("отпуск", "отпуск"),
    )

    private suspend fun seed() {
        accounts.saveAccount(Account("card", "Карта", ars))
        categories.saveCategory(Category("cafe", "Кафе", CategoryType.EXPENSE))
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
        entryTemplates = fakeEntryTemplates(accounts, categories),
        entryTags = fakeEntryTags(tags),
    )

    @Test
    fun `chosen tags are saved as labels of a new operation`() = runTest {
        seed()
        val vm = viewModel()
        vm.onAmountChange("100")
        vm.onCategorySelect("cafe")
        vm.onTagToggle("отпуск")
        vm.onTagToggle("аня")
        vm.onTagToggle("отпуск")
        vm.save()
        assertEquals(listOf("аня"), transactions.getTransaction("new")!!.labels)
    }

    @Test
    fun `editing keeps the original form of a label and a reference to a deleted tag`() = runTest {
        seed()
        transactions.saveTransaction(
            Transaction(
                "t", TransactionType.EXPENSE, "card", Money(100_00, ars), now, categoryId = "cafe",
                labels = listOf("Mercado Libre", "старый тег"),
            ),
        )
        val vm = viewModel("t")
        assertEquals(listOf("mercado libre", "старый тег"), vm.state.value.tagIds)
        vm.onTagToggle("аня")
        vm.save()
        assertEquals(listOf("Mercado Libre", "старый тег", "аня"), transactions.getTransaction("t")!!.labels)
    }

    @Test
    fun `a new tag is created and chosen, a taken name picks the existing tag`() = runTest {
        seed()
        val vm = viewModel()
        vm.onTagCreate("  бассейн ", 0xFF00FFAAL)
        assertEquals(listOf("бассейн"), vm.state.value.tagIds)
        assertEquals(0xFF00FFAAL, tags.getTag("бассейн")!!.color)
        vm.onTagCreate("АНЯ", null)
        assertEquals(listOf("бассейн", "аня"), vm.state.value.tagIds)
        assertEquals(4, tags.getTags().size)
        assertEquals(true, vm.state.value.tags.options.any { it.id == "бассейн" })
    }
}

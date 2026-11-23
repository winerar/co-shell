package app.coshell.transactions.edit

import app.coshell.transactions.edit.TransactionEditUiState.EntryStep
import app.coshell.testing.FakeTemplateUsage
import app.coshell.testing.FakeTipSettings
import app.coshell.testing.fakeEntryTags
import app.coshell.testing.fakeEntryTip
import app.coshell.testing.fakeEntryTemplates
import app.coshell.testing.FakeTemplateRepository
import app.coshell.core.model.TransactionTemplate
import androidx.lifecycle.SavedStateHandle
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.ai.NoLanguageModel
import app.coshell.core.domain.ai.PayeeCategoryAi
import app.coshell.core.domain.drafts.InMemoryDraftRepository
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


/** Shared accounts, categories and the view model factory of the entry sheet tests (#136). */
abstract class TransactionEditTestBase {
    @get:Rule val main = MainDispatcherRule()

    protected val zone = TimeZone.of("America/Argentina/Buenos_Aires")
    protected val now = LocalDateTime(2026, 11, 8, 14, 30).toInstant(zone)
    protected val ars = CurrencyCode("ARS")
    protected val usd = CurrencyCode("USD")

    protected val accounts = FakeAccountRepository()
    protected val categories = FakeCategoryRepository()
    protected val transactions = FakeTransactionRepository()
    protected val drafts = InMemoryDraftRepository()

    protected suspend fun seed() {
        accounts.saveAccount(Account("card", "Карта", ars, sortOrder = 0))
        accounts.saveAccount(Account("cash", "Наличные", usd, sortOrder = 1))
        accounts.saveAccount(Account("old", "Старый", ars, archived = true))
        categories.saveCategory(Category("food", "Еда", CategoryType.EXPENSE))
        categories.saveCategory(Category("cafe", "Кафе", CategoryType.EXPENSE, parentId = "food"))
        categories.saveCategory(Category("salary", "Зарплата", CategoryType.INCOME))
    }

    protected val rates = FakeExchangeRateRepository()
    protected val tipSettings = FakeTipSettings()

    protected val templates = FakeTemplateRepository()
    protected val usage = FakeTemplateUsage()

    protected fun viewModel(
        transactionId: String? = null,
        type: String = "EXPENSE",
        copyFrom: String? = null,
        templateId: String? = null,
        receiptQr: String? = null,
        receiptUnread: Boolean = false,
        draftId: String? = null,
        categoryAi: PayeeCategoryAi = PayeeCategoryAi(NoLanguageModel),
    ) = TransactionEditViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(
                TransactionEditDestination.ARG_TRANSACTION_ID to transactionId,
                TransactionEditDestination.ARG_TYPE to type,
                TransactionEditDestination.ARG_COPY_FROM to copyFrom,
                TransactionEditDestination.ARG_TEMPLATE_ID to templateId,
                TransactionEditDestination.ARG_RECEIPT_QR to receiptQr,
                TransactionEditDestination.ARG_RECEIPT_UNREAD to receiptUnread,
                TransactionEditDestination.ARG_DRAFT_ID to draftId,
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
        rates = rates,
        deleteWithUndo = DeleteTransactionWithUndo(transactions, UndoCenter()),
        entryTemplates = fakeEntryTemplates(accounts, categories, templates, usage),
        entryTags = fakeEntryTags(),
        entryTip = fakeEntryTip(categories, transactions, tipSettings),
        drafts = drafts,
        categoryAi = categoryAi,
    )

    protected suspend fun seedTemplate(amount: Money? = Money(40_000_00, ars), categoryId: String? = "cafe") {
        seed()
        templates.saveTemplate(
            TransactionTemplate("spanish", "Испанский", TransactionType.EXPENSE, "card", ars, amount, categoryId, note = "Марта"),
        )
    }
}

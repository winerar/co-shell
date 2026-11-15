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
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import app.coshell.navigation.TransactionEditDestination
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeTemplateRepository
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
import app.coshell.testing.fakeEntryTags
import app.coshell.testing.fakeEntryTemplates
import app.coshell.transactions.DeleteTransactionWithUndo
import app.coshell.undo.UndoCenter
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** «Провести» from «Скоро» on «Обзоре» (#143): the sheet on the payment's day, the payment handled once saved. */
class TransactionEditPaymentTest {
    @get:Rule val main = MainDispatcherRule()

    private val zone = TimeZone.of("America/Argentina/Buenos_Aires")
    private val now = LocalDateTime(2026, 11, 15, 10, 0).toInstant(zone)
    private val ars = CurrencyCode("ARS")

    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()
    private val templates = FakeTemplateRepository()

    /** «Уборка» every Friday from 6 November, nothing handled: 6 and 13 November are missed. */
    private suspend fun seed() {
        accounts.saveAccount(Account("cash", "Наличные", ars))
        categories.saveCategory(Category("home", "Дом", CategoryType.EXPENSE))
        categories.saveCategory(Category("food", "Еда", CategoryType.EXPENSE))
        templates.saveTemplate(
            TransactionTemplate(
                "cleaning", "Уборка", TransactionType.EXPENSE, "cash", ars, Money(30_000_00, ars), "home",
                schedule = RecurringSchedule(Recurrence.Weekly(kotlinx.datetime.DayOfWeek.FRIDAY), start = LocalDate(2026, 11, 6)),
            ),
        )
        templates.saveTemplate(TransactionTemplate("coffee", "Кофе", TransactionType.EXPENSE, "cash", ars, Money(3_000_00, ars), "food"))
    }

    private fun viewModel(templateId: String?, paymentDate: String?) = TransactionEditViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(
                TransactionEditDestination.ARG_TEMPLATE_ID to templateId,
                TransactionEditDestination.ARG_PAYMENT_DATE to paymentDate,
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
        entryTemplates = fakeEntryTemplates(accounts, categories, templates),
        entryTags = fakeEntryTags(),
    )

    private suspend fun handledThrough() = templates.getTemplate("cleaning")!!.schedule!!.handledThrough

    @Test
    fun `the sheet opens on the payment day and saving marks it handled`() = runTest {
        seed()
        val vm = viewModel("cleaning", "2026-11-06")
        assertEquals(LocalDate(2026, 11, 6), vm.state.value.date)

        vm.save()

        assertEquals(LocalDate(2026, 11, 6), transactions.getTransaction("new")!!.date.toLocalDateTime(zone).date)
        // Only the oldest one: 13 November still waits in «Скоро».
        assertEquals(LocalDate(2026, 11, 6), handledThrough())
    }

    @Test
    fun `an operation that is not saved leaves the payment due`() = runTest {
        seed()
        val vm = viewModel("cleaning", "2026-11-13")
        vm.onAmountChange("")
        vm.save()

        assertNull(handledThrough())
    }

    @Test
    fun `another template picked in the strip is an ordinary operation`() = runTest {
        seed()
        val vm = viewModel("cleaning", "2026-11-13")

        vm.onTemplatePick("coffee")
        assertEquals(LocalDate(2026, 11, 15), vm.state.value.date)
        vm.save()

        assertNull(handledThrough())
    }

    @Test
    fun `clearing the template forgets the payment`() = runTest {
        seed()
        val vm = viewModel("cleaning", "2026-11-13")

        vm.onTemplateClear()
        vm.onAccountSelect("cash")
        vm.onAmountChange("10")
        vm.onCategorySelect("food")
        vm.save()

        assertNull(handledThrough())
    }
}

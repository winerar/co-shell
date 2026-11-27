package app.coshell.transactions.offer

import app.coshell.testing.MainDispatcherRule
import app.coshell.core.domain.settings.InMemorySuggestionDismissals
import app.coshell.core.domain.templates.SaveTemplateUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Recurrence
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeTemplateRepository
import app.coshell.testing.FakeTransactionRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** «Сделать регулярным?» (#789): the card from three monthly payments, the template it makes, «Не сейчас». */
class RecurringOfferViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val ars = CurrencyCode("ARS")
    private val accounts = FakeAccountRepository()
    private val transactions = FakeTransactionRepository()
    private val templates = FakeTemplateRepository()
    private val dismissals = InMemorySuggestionDismissals()

    private suspend fun viewModel(): RecurringOfferViewModel {
        accounts.saveAccount(Account("galicia", "Galicia", ars))
        listOf(LocalDate(2026, 9, 5), LocalDate(2026, 10, 5), LocalDate(2026, 11, 5)).forEachIndexed { i, day ->
            transactions.saveTransaction(
                Transaction("t$i", TransactionType.EXPENSE, "galicia", Money(25_000_00, ars), day.atStartOfDayIn(TimeZone.UTC), payee = "Telecentro"),
            )
        }
        return RecurringOfferViewModel(
            transactions = transactions,
            categories = FakeCategoryRepository(),
            templates = templates,
            saveTemplate = SaveTemplateUseCase(accounts, FakeCategoryRepository(), templates),
            dismissals = dismissals,
            ids = IdGenerator { "internet" },
            moneyFormat = MoneyFormat.RU,
            clock = object : Clock { override fun now() = Instant.parse("2026-11-27T12:00:00Z") },
            timeZone = TimeZone.UTC,
        )
    }

    @Test
    fun `three monthly payments make an offer and accepting saves a template with a schedule`() = runTest {
        val vm = viewModel()
        vm.offer.launchIn(backgroundScope)
        val offer = vm.offer.first { it != null }!!
        assertEquals("Telecentro", offer.name)
        assertEquals(5, offer.day)

        vm.accept()
        assertEquals("internet", vm.opened.first())
        val saved = templates.getTemplate("internet")!!
        assertEquals(Recurrence.Monthly(5), saved.schedule?.recurrence)
        assertEquals(LocalDate(2026, 12, 5), saved.schedule?.start)
        assertEquals(Money(25_000_00, ars), saved.amount)
        // The schedule covers it now: no more offer.
        assertNull(vm.offer.first { it == null })
    }

    @Test
    fun `not now hides the offer`() = runTest {
        val vm = viewModel()
        vm.offer.launchIn(backgroundScope)
        vm.offer.first { it != null }
        vm.later()
        assertNull(vm.offer.first { it == null })
    }
}

package app.coshell.core.domain.recurring

import app.coshell.core.domain.FakeAccountRepository
import app.coshell.core.domain.FakeCategoryRepository
import app.coshell.core.domain.FakeExchangeRateRepository
import app.coshell.core.domain.FakeTemplateRepository
import app.coshell.core.domain.FakeTransactionRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.templates.draftFrom
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.PostMode
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

/** «Аренда · 650 USD» from an ARS account (#415): debited by the rate of the day, the template's amount is the receipt. */
class ForeignPaymentsTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val now = Instant.parse("2026-11-15T15:00:00Z")
    private val clock = object : Clock {
        override fun now() = now
    }
    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val templates = FakeTemplateRepository()
    private val transactions = FakeTransactionRepository()
    private val rates = FakeExchangeRateRepository()
    private val save = SaveTransactionUseCase(accounts, categories, transactions)
    private val postAuto = PostAutoPaymentsUseCase(accounts, categories, templates, save, rates, clock)
    private val postOne = PostPaymentUseCase(accounts, categories, templates, save, rates, clock)
    private val missed = MissedPaymentsUseCase(templates, postOne)

    private fun d(s: String) = LocalDate.parse(s)
    private fun rate(date: String, value: String) = ExchangeRate(d(date), usd, ars, Rate.parse(value), RateOrigin.DOWNLOADED)
    private fun rent(mode: PostMode = PostMode.AUTO, handled: String = "2026-10-10") = TransactionTemplate(
        "rent", "Аренда", TransactionType.EXPENSE, "galicia", ars, Money(650_00, usd),
        schedule = RecurringSchedule(Recurrence.Monthly(10), d("2026-01-01"), mode, d(handled)),
    )

    @BeforeTest
    fun setUp() = runTest {
        accounts.saveAccount(Account("galicia", "Galicia", ars))
    }

    @Test
    fun `auto debits by the rate of the payment day and keeps the receipt`() = runTest {
        rates.rates.value = listOf(rate("2026-11-09", "1000"), rate("2026-11-10", "1050"), rate("2026-11-14", "1100"))
        templates.saveTemplate(rent())

        val tx = postAuto(TimeZone.UTC).single().transaction

        assertEquals("rec-rent-2026-11-10", tx.id)
        assertEquals(Money(682_500_00, ars), tx.amount)
        assertEquals(Money(650_00, usd), tx.originalAmount)
        assertEquals(d("2026-11-10"), templates.getTemplate("rent")?.schedule?.handledThrough)
    }

    @Test
    fun `without any rate nothing posts by itself and the payment stays due`() = runTest {
        templates.saveTemplate(rent())

        assertEquals(emptyList(), postAuto(TimeZone.UTC))
        assertNull(postOne("rent", d("2026-11-10"), TimeZone.UTC), "«Провести 650 USD» on the reminder waits for the sheet")
        assertTrue(transactions.items.value.isEmpty())
        assertEquals(d("2026-10-10"), templates.getTemplate("rent")?.schedule?.handledThrough)
    }

    @Test
    fun `reminder button and post-all use the rate of each day`() = runTest {
        rates.rates.value = listOf(rate("2026-09-01", "900"), rate("2026-10-10", "1000"))
        templates.saveTemplate(rent(PostMode.REMIND, handled = "2026-09-10"))

        assertEquals(1, missed.postAll("rent", today = d("2026-11-09"), timeZone = TimeZone.UTC))
        assertEquals(Money(650_000_00, ars), transactions.items.value.values.single().amount)

        val posted = postOne("rent", d("2026-11-10"), TimeZone.UTC)
        assertEquals(Money(650_000_00, ars), posted?.transaction?.amount, "the nearest earlier rate serves the day")
        assertEquals(Money(650_00, usd), posted?.transaction?.originalAmount)
    }

    @Test
    fun `the sheet gets the receipt, not an amount in the account currency`() {
        val draft = draftFrom(rent(), now, accounts.items.value.getValue("galicia"), target = null, category = null)
        assertNull(draft.amount)
        assertEquals(Money(650_00, usd), draft.receipt)

        val noAccount = draftFrom(rent(), now, account = null, target = null, category = null)
        assertNull(noAccount.receipt)
    }
}

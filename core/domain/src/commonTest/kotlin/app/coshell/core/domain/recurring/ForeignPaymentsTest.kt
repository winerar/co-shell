package app.coshell.core.domain.recurring

import app.coshell.core.domain.FakeAccountRepository
import app.coshell.core.domain.FakeCategoryRepository
import app.coshell.core.domain.FakeTemplateRepository
import app.coshell.core.domain.FakeTransactionRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.templates.draftFrom
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.PostMode
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * «Аренда · 650 USD» from an ARS account (#415): the template's amount is the receipt; what was debited is always
 * entered by the user in the sheet (owner's poll 3 «Д») — nothing posts by itself or from the reminder's button.
 */
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
    private val save = SaveTransactionUseCase(accounts, categories, transactions)
    private val postAuto = PostAutoPaymentsUseCase(accounts, categories, templates, save, clock)
    private val postOne = PostPaymentUseCase(accounts, categories, templates, save, clock)
    private val missed = MissedPaymentsUseCase(templates, postOne)

    private fun d(s: String) = LocalDate.parse(s)
    private fun rent(mode: PostMode = PostMode.AUTO, handled: String = "2026-10-10") = TransactionTemplate(
        "rent", "Аренда", TransactionType.EXPENSE, "galicia", ars, Money(650_00, usd),
        schedule = RecurringSchedule(Recurrence.Monthly(10), d("2026-01-01"), mode, d(handled)),
    )

    @BeforeTest
    fun setUp() = runTest {
        accounts.saveAccount(Account("galicia", "Galicia", ars))
    }

    @Test
    fun `a payment in another currency never posts by itself, even stored as AUTO`() = runTest {
        templates.saveTemplate(rent())

        assertFalse(rent().postsByItself)
        assertEquals(emptyList(), postAuto(TimeZone.UTC))
        assertTrue(transactions.items.value.isEmpty())
        assertEquals(d("2026-10-10"), templates.getTemplate("rent")?.schedule?.handledThrough, "the payment stays due for the sheet")
    }

    @Test
    fun `the reminder button and post-all leave it to the sheet`() = runTest {
        templates.saveTemplate(rent(PostMode.REMIND, handled = "2026-09-10"))

        assertNull(postOne("rent", d("2026-11-10"), TimeZone.UTC))
        assertEquals(0, missed.postAll("rent", today = d("2026-11-11"), timeZone = TimeZone.UTC))
        assertTrue(transactions.items.value.isEmpty())
        assertEquals(d("2026-09-10"), templates.getTemplate("rent")?.schedule?.handledThrough)
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

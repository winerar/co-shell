package app.coshell.core.domain.recurring

import app.coshell.core.domain.FakeAccountRepository
import app.coshell.core.domain.FakeCategoryRepository
import app.coshell.core.domain.FakeExchangeRateRepository
import app.coshell.core.domain.FakeTemplateRepository
import app.coshell.core.domain.FakeTransactionRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
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

class RecurringUseCasesTest {
    private val ars = CurrencyCode("ARS")
    private val now = Instant.parse("2026-11-15T15:00:00Z") // Sunday
    private val clock = object : Clock {
        override fun now() = now
    }
    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val templates = FakeTemplateRepository()
    private val transactions = FakeTransactionRepository()
    private val post = PostAutoPaymentsUseCase(accounts, categories, templates, SaveTransactionUseCase(accounts, categories, transactions), FakeExchangeRateRepository(), clock)
    private val mark = MarkPaymentsHandledUseCase(templates)
    private val postOne = PostPaymentUseCase(accounts, categories, templates, SaveTransactionUseCase(accounts, categories, transactions), FakeExchangeRateRepository(), clock)

    private fun d(s: String) = LocalDate.parse(s)
    private fun netflix(mode: PostMode = PostMode.AUTO, handled: String? = "2026-09-18") = TransactionTemplate(
        "netflix", "Netflix", TransactionType.EXPENSE, "usdt", ars, Money(7_990_00, ars), categoryId = "subs", note = "Netflix",
        schedule = RecurringSchedule(Recurrence.Monthly(18), d("2026-01-01"), mode, handled?.let(::d)),
    )

    @BeforeTest
    fun setUp() = runTest {
        accounts.saveAccount(Account("usdt", "USDT-карта", ars))
        categories.saveCategory(Category("subs", "Подписки", CategoryType.EXPENSE))
    }

    @Test
    fun `auto posts every due payment once, dated its day, and marks it handled`() = runTest {
        templates.saveTemplate(netflix())
        val posted = post(TimeZone.UTC, authorMemberId = "me")
        assertEquals(listOf("rec-netflix-2026-10-18"), posted.map { it.transaction.id })
        assertEquals(Instant.parse("2026-10-18T12:00:00Z"), posted.single().transaction.date)
        assertEquals("me", posted.single().transaction.authorMemberId)
        assertEquals("Netflix", posted.single().template.name)
        assertEquals(d("2026-10-18"), templates.getTemplate("netflix")?.schedule?.handledThrough)
        assertEquals(emptyList(), post(TimeZone.UTC))
        assertEquals(1, transactions.items.value.size)
    }

    @Test
    fun `remind rules and templates without repeat are not posted`() = runTest {
        templates.saveTemplate(netflix(PostMode.REMIND))
        assertEquals(emptyList(), post(TimeZone.UTC))
        assertTrue(transactions.items.value.isEmpty())
    }

    @Test
    fun `archived account leaves the payment due for a hand post`() = runTest {
        templates.saveTemplate(netflix())
        accounts.saveAccount(Account("usdt", "USDT-карта", ars, archived = true))
        assertEquals(emptyList(), post(TimeZone.UTC))
        assertEquals(d("2026-09-18"), templates.getTemplate("netflix")?.schedule?.handledThrough)
    }

    @Test
    fun `mark handled advances the rule, unknown template is refused`() = runTest {
        templates.saveTemplate(netflix(PostMode.REMIND))
        assertTrue(mark("netflix", d("2026-10-18")))
        assertEquals(d("2026-11-18"), templates.getTemplate("netflix")?.schedule?.nextUnhandled())
        assertFalse(mark("nope", d("2026-10-18")))
    }

    @Test
    fun `post from the reminder posts one payment by the template amount and marks it handled`() = runTest {
        templates.saveTemplate(netflix(PostMode.REMIND, handled = "2026-10-18"))
        val posted = postOne("netflix", d("2026-11-15"), TimeZone.UTC, authorMemberId = "me")!!
        assertEquals("rec-netflix-2026-11-15", posted.transaction.id)
        assertEquals(now, posted.transaction.date)
        assertEquals(Money(7_990_00, ars), posted.transaction.amount)
        assertEquals(d("2026-11-15"), templates.getTemplate("netflix")?.schedule?.handledThrough)
    }

    @Test
    fun `post from the reminder refuses a template without amount or with an archived account`() = runTest {
        templates.saveTemplate(netflix(PostMode.REMIND).copy(amount = null))
        assertNull(postOne("netflix", d("2026-10-18"), TimeZone.UTC))
        templates.saveTemplate(netflix(PostMode.REMIND))
        accounts.saveAccount(Account("usdt", "USDT-карта", ars, archived = true))
        assertNull(postOne("netflix", d("2026-10-18"), TimeZone.UTC))
        assertNull(postOne("nope", d("2026-10-18"), TimeZone.UTC))
        assertTrue(transactions.items.value.isEmpty())
        assertEquals(d("2026-09-18"), templates.getTemplate("netflix")?.schedule?.handledThrough)
    }
}

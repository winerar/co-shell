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
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
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

/** Special cases of regular payments (#406, board `Rec143Edge`): missed, archived account, two phones. */
class EdgeCasesTest {
    private val ars = CurrencyCode("ARS")
    private val today = LocalDate(2026, 11, 15) // Sunday
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-15T15:00:00Z")
    }
    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val templates = FakeTemplateRepository()
    private val transactions = FakeTransactionRepository()
    private val save = SaveTransactionUseCase(accounts, categories, transactions)
    private val missed = MissedPaymentsUseCase(templates, PostPaymentUseCase(accounts, categories, templates, save, FakeExchangeRateRepository(), clock))

    private val cash = Account("cash", "Наличные", ars)
    private val card = Account("card", "Galicia", ars)

    /** «Уборка» every Friday, last handled on 30 October: 6 and 13 November are missed. */
    private fun cleaning(amount: Money? = Money(30_000_00, ars), category: String? = "home") = TransactionTemplate(
        "clean", "Уборка", TransactionType.EXPENSE, "cash", ars, amount, categoryId = category,
        schedule = RecurringSchedule(Recurrence.Weekly(DayOfWeek.FRIDAY), LocalDate(2026, 1, 1), handledThrough = LocalDate(2026, 10, 30)),
    )

    private fun rent(account: String = "cash") = TransactionTemplate(
        "rent", "Аренда", TransactionType.EXPENSE, account, ars, Money(650_000_00, ars),
        schedule = RecurringSchedule(Recurrence.Monthly(5), LocalDate(2026, 1, 1), handledThrough = LocalDate(2026, 10, 5)),
    )

    @BeforeTest
    fun setUp() = runTest {
        accounts.saveAccount(cash)
        accounts.saveAccount(card)
        categories.saveCategory(Category("home", "Дом", CategoryType.EXPENSE))
    }

    @Test
    fun `missed payments pile up and are listed oldest first`() {
        assertEquals(listOf(LocalDate(2026, 11, 6), LocalDate(2026, 11, 13)), cleaning().missedDays(today))
        assertEquals(2, upcomingPayments(listOf(cleaning()), today).single().missed.size)
    }

    @Test
    fun `post all posts every missed payment by its day and marks them handled`() = runTest {
        templates.saveTemplate(cleaning())
        assertEquals(2, missed.postAll("clean", today, TimeZone.UTC))
        assertEquals(
            setOf("rec-clean-2026-11-06", "rec-clean-2026-11-13"),
            transactions.items.value.keys,
        )
        assertEquals(LocalDate(2026, 11, 13), templates.getTemplate("clean")?.schedule?.handledThrough)
        assertTrue(upcomingPayments(templates.items.value.values.toList(), today).none { it.state == PaymentState.MISSED })
    }

    @Test
    fun `post all without an amount posts nothing - the sheet takes over`() = runTest {
        templates.saveTemplate(cleaning(amount = null))
        assertEquals(0, missed.postAll("clean", today, TimeZone.UTC))
        assertTrue(transactions.items.value.isEmpty())
        assertEquals(2, templates.getTemplate("clean")!!.missedDays(today).size)
    }

    @Test
    fun `post all stops when the category is archived and keeps the rest missed`() = runTest {
        templates.saveTemplate(cleaning())
        categories.saveCategory(Category("home", "Дом", CategoryType.EXPENSE, archived = true))
        assertEquals(0, missed.postAll("clean", today, TimeZone.UTC))
        assertEquals(2, templates.getTemplate("clean")!!.missedDays(today).size)
    }

    @Test
    fun `skip all handles the missed ones without operations, today still waits`() = runTest {
        val friday = LocalDate(2026, 11, 20)
        templates.saveTemplate(cleaning())
        assertTrue(missed.skipAll("clean", friday)) // 6, 13 missed; 20 is today
        assertTrue(transactions.items.value.isEmpty())
        val template = templates.getTemplate("clean")!!
        assertEquals(LocalDate(2026, 11, 13), template.schedule?.handledThrough)
        assertEquals(listOf(friday), template.schedule?.due(friday))
        assertFalse(missed.skipAll("clean", friday))
    }

    @Test
    fun `archived account pauses the payment with the account name`() {
        val byId = mapOf("cash" to cash.copy(archived = true), "card" to card)
        assertEquals(PausedPayment(rent(), "Наличные"), pausedPayment(rent(), byId))
        assertNull(pausedPayment(rent("card"), byId))
        assertEquals(listOf(rent("card")), activePayments(listOf(rent(), rent("card")), byId))
    }

    @Test
    fun `deleted account or archived transfer target pauses too`() {
        assertEquals(PausedPayment(rent(), null), pausedPayment(rent(), mapOf("card" to card)))
        val transfer = TransactionTemplate(
            "save", "Копилка", TransactionType.TRANSFER, "card", ars, toAccountId = "cash",
            schedule = RecurringSchedule(Recurrence.Monthly(1), LocalDate(2026, 1, 1)),
        )
        assertEquals("Наличные", pausedPayment(transfer, mapOf("card" to card, "cash" to cash.copy(archived = true)))?.accountName)
    }

    @Test
    fun `a template without repeat is never paused`() {
        val plain = rent().copy(schedule = null)
        assertNull(pausedPayment(plain, emptyMap()))
        assertEquals(emptyList(), pausedPayments(listOf(plain), emptyMap()))
    }

    @Test
    fun `payment handled on the other phone drops out of the reminder`() {
        val light = TransactionTemplate(
            "light", "Свет", TransactionType.EXPENSE, "cash", ars,
            schedule = RecurringSchedule(Recurrence.Monthly(15), LocalDate(2026, 1, 1), handledThrough = LocalDate(2026, 10, 15)),
        )
        val gas = light.copy(id = "gas", name = "Газ")
        val reminded = setOf("light", "gas")
        assertEquals(listOf("Газ", "Свет"), stillToRemind(reminded, listOf(light, gas), today).map { it.name })
        // The other member posted «Свет»: its handledThrough came by sync.
        val synced = light.copy(schedule = light.schedule!!.handledUpTo(today))
        assertEquals(listOf("Газ"), stillToRemind(reminded, listOf(synced, gas), today).map { it.name })
        assertEquals(emptyList(), stillToRemind(reminded, listOf(synced), today))
        assertEquals(emptyList(), stillToRemind(emptySet(), listOf(light, gas), today))
    }
}

package app.coshell.recurring.list

import app.coshell.core.domain.recurring.PaymentState
import app.coshell.core.domain.recurring.scheduledPayments
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.PostMode
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import app.coshell.templates.toItem
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Регулярные платежи» (#143, #403, board `Rec143List`); today — Sunday 15 November 2026. */
class RecurringListTest {
    private val today = LocalDate(2026, 11, 15)
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val rub = CurrencyCode("RUB")
    private val accounts = mapOf(
        "galicia" to Account("galicia", "Galicia", ars),
        "cash" to Account("cash", "Наличные", usd),
        "box" to Account("box", "Копилка", usd),
        "tbank" to Account("tbank", "Т-Банк", rub),
    )

    private fun rule(
        name: String,
        recurrence: Recurrence,
        amount: Money? = null,
        type: TransactionType = TransactionType.EXPENSE,
        account: String = "galicia",
        mode: PostMode = PostMode.REMIND,
        handled: LocalDate? = null,
    ) = TransactionTemplate(
        id = name,
        name = name,
        type = type,
        accountId = account,
        currency = accounts.getValue(account).currency,
        amount = amount,
        toAccountId = if (type == TransactionType.TRANSFER) "box" else null,
        schedule = RecurringSchedule(recurrence, LocalDate(2026, 1, 1), mode, handled),
    )

    private val templates = listOf(
        rule("Уборка", Recurrence.Weekly(DayOfWeek.FRIDAY), Money(30_000_00, ars), handled = LocalDate(2026, 11, 6)),
        rule("Свет", Recurrence.Monthly(15), handled = LocalDate(2026, 10, 15)),
        rule("Netflix", Recurrence.Monthly(18), Money(7_990_00, ars), mode = PostMode.AUTO, handled = LocalDate(2026, 10, 18)),
        rule("Зарплата", Recurrence.Monthly(1), Money(1_200_00, usd), TransactionType.INCOME, "cash", handled = LocalDate(2026, 11, 1)),
        rule("Копилка", Recurrence.Monthly(5), Money(100_00, usd), TransactionType.TRANSFER, "cash", handled = LocalDate(2026, 11, 5)),
    )

    private fun list(query: String = "", filter: PaymentFilter = PaymentFilter.ALL, from: List<TransactionTemplate> = templates) =
        recurringList(
            payments = scheduledPayments(from, today),
            items = from.associate { it.id to it.toItem(accounts, emptyMap(), MoneyFormat.RU) },
            ahead = null,
            query = query,
            filter = filter,
        )

    @Test
    fun `rules are grouped by the month of the next payment, by date`() {
        val state = list()
        assertEquals(5, state.total)
        assertEquals(listOf(YearMonth(2026, Month.NOVEMBER), YearMonth(2026, Month.DECEMBER)), state.months.map { it.month })
        assertEquals(listOf("Уборка", "Свет", "Netflix"), state.months[0].rows.map { it.item.name })
        assertEquals(listOf("Зарплата", "Копилка"), state.months[1].rows.map { it.item.name })
        val cleaning = state.months[0].rows[0]
        assertEquals(PaymentState.MISSED, cleaning.state)
        assertEquals(1, cleaning.missed)
        assertTrue(state.months[0].rows[2].auto)
    }

    @Test
    fun `chips count rules and hide empty ones`() {
        val counts = list().counts
        assertEquals(
            mapOf(PaymentFilter.ALL to 5, PaymentFilter.EXPENSE to 3, PaymentFilter.INCOME to 1, PaymentFilter.TRANSFER to 1, PaymentFilter.AUTO to 1),
            counts,
        )
        val onlyExpenses = list(from = templates.take(2)).counts
        assertEquals(mapOf(PaymentFilter.ALL to 2, PaymentFilter.EXPENSE to 2), onlyExpenses)
    }

    @Test
    fun `filter and search narrow the rows`() {
        assertEquals(listOf("Netflix"), list(filter = PaymentFilter.AUTO).months.flatMap { it.rows }.map { it.item.name })
        assertEquals(listOf("Зарплата", "Копилка"), list(query = "наличн").months.flatMap { it.rows }.map { it.item.name })
        assertTrue(list(query = "газ").nothingFound)
    }

    @Test
    fun `a chip with no rules left falls back to all`() {
        val state = list(filter = PaymentFilter.AUTO, from = templates.take(2))
        assertEquals(PaymentFilter.ALL, state.filter)
        assertEquals(2, state.months.flatMap { it.rows }.size)
    }

    @Test
    fun `no rules is the empty screen`() {
        assertTrue(list(from = emptyList()).isEmpty)
    }

    @Test
    fun `hero sums the payments left this month in the main currency`() {
        val rates = listOf(ExchangeRate(today, usd, ars, Rate.parse("1500"), RateOrigin.DOWNLOADED))
        // Уборка 13, 20, 27 Nov (3 x 30 000 ARS) + Netflix 18 Nov (7 990 ARS) = 97 990 ARS = 65,33 USD; Свет — on entry.
        val ahead = monthAheadUi(templates, rates, usd, today, MoneyFormat.RU)
        assertEquals(YearMonth(2026, Month.NOVEMBER), ahead.month)
        assertEquals(5, ahead.count)
        assertEquals(1, ahead.withoutAmount)
        assertEquals("−" + MoneyFormat.RU.format(Money(65_33, usd)), ahead.total)
        assertTrue(ahead.missing.isEmpty())
    }

    @Test
    fun `hero names currencies without a rate`() {
        val books = listOf(rule("Книги", Recurrence.Monthly(20), Money(1_000_00, rub), account = "tbank", handled = LocalDate(2026, 10, 20)))
        val ahead = monthAheadUi(books, emptyList(), usd, today, MoneyFormat.RU)
        assertNull(ahead.total)
        assertEquals(listOf("RUB"), ahead.missing)
    }

    @Test
    fun `paused payments are shown above the list and survive search and chips`() {
        val paused = listOf(PausedUi("Аренда", "Аренда", "Наличные"))
        val state = recurringList(
            payments = scheduledPayments(templates, today),
            items = templates.associate { it.id to it.toItem(accounts, emptyMap(), MoneyFormat.RU) },
            ahead = null,
            query = "нет такого",
            filter = PaymentFilter.INCOME,
            paused = paused,
        )
        assertEquals(paused, state.paused)
    }
}

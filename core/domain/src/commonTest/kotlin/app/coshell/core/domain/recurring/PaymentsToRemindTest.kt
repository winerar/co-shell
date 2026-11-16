package app.coshell.core.domain.recurring

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PaymentsToRemindTest {
    private val today = LocalDate(2026, 11, 15)

    private fun rule(name: String, day: Int, handled: LocalDate? = null, start: LocalDate = LocalDate(2026, 1, 1)) =
        TransactionTemplate(
            name, name, TransactionType.EXPENSE, "cash", CurrencyCode("ARS"),
            schedule = RecurringSchedule(Recurrence.Monthly(day), start, handledThrough = handled),
        )

    @Test
    fun `only payments due today and not handled, by name`() {
        val templates = listOf(
            rule("Свет", 15),
            rule("Аренда", 15),
            rule("Газ", 10), // missed, not today
            rule("Интернет", 15, handled = today), // already posted
            rule("Уборка", 15, start = LocalDate(2026, 12, 1)), // starts later
            TransactionTemplate("coffee", "Кофе", TransactionType.EXPENSE, "cash", CurrencyCode("ARS")),
        )
        assertEquals(listOf("Аренда", "Свет"), paymentsToRemind(templates, today).map { it.name })
    }

    @Test
    fun `missed earlier and due today again is reminded`() {
        val templates = listOf(rule("Свет", 15, handled = LocalDate(2026, 9, 15)))
        assertEquals(listOf("Свет"), paymentsToRemind(templates, today).map { it.name })
    }

    @Test
    fun `tomorrow on yesterday's reminder asks again while the payment waits`() {
        val templates = listOf(
            rule("Свет", 14), // put off yesterday, still waiting
            rule("Газ", 14, handled = LocalDate(2026, 11, 14)), // put off, then posted by hand
            rule("Вода", 14), // not put off: missed, not reminded
        )
        assertEquals(listOf("Свет"), paymentsToRemind(templates, today, snoozed = setOf("Свет", "Газ")).map { it.name })
    }

    @Test
    fun `the total of reminded payments in the main currency, by size, without unknown ones`() {
        val usd = CurrencyCode("USD")
        val ars = CurrencyCode("ARS")
        val book = CurrencyConverter(RateBook.of(listOf(ExchangeRate(today, usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED))))
        val templates = listOf(
            rule("Интернет", 15).copy(amount = Money(40_000_00, ars)),
            rule("Аренда", 15).copy(currency = usd, amount = Money(650_00, usd)),
            rule("Зарплата", 15).copy(type = TransactionType.INCOME, currency = usd, amount = Money(10_00, usd)),
            rule("Свет", 15), // amount when posting
            rule("Драмы", 15).copy(currency = CurrencyCode("AMD"), amount = Money(5_000_00, CurrencyCode("AMD"))), // no rate
        )
        assertEquals(Money(700_00, usd), remindTotal(templates, book, usd, today))
        assertNull(remindTotal(listOf(rule("Свет", 15)), book, usd, today))
    }
}

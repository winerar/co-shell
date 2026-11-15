package app.coshell.core.domain.recurring

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OccurrencesTest {
    private fun d(s: String) = LocalDate.parse(s)
    private val ars = CurrencyCode("ARS")

    @Test
    fun `monthly day 31 falls on the last day of a short month`() {
        val r = Recurrence.Monthly(31)
        assertEquals(d("2026-11-30"), r.firstOnOrAfter(d("2026-11-15")))
        assertEquals(d("2027-02-28"), r.firstOnOrAfter(d("2027-02-01")))
        assertEquals(d("2028-02-29"), r.firstOnOrAfter(d("2028-02-01")))
        assertEquals(listOf(d("2027-01-31"), d("2027-02-28"), d("2027-03-31")), r.between(d("2027-01-01"), d("2027-03-31")))
    }

    @Test
    fun `monthly day already passed moves to next month, across the year`() {
        assertEquals(d("2026-12-05"), Recurrence.Monthly(5).firstOnOrAfter(d("2026-11-15")))
        assertEquals(d("2027-01-05"), Recurrence.Monthly(5).firstOnOrAfter(d("2026-12-06")))
        assertEquals(d("2026-11-15"), Recurrence.Monthly(15).firstOnOrAfter(d("2026-11-15")))
    }

    @Test
    fun `weekly finds the weekday on or after`() {
        // 2026-11-15 is a Sunday
        assertEquals(d("2026-11-20"), Recurrence.Weekly(DayOfWeek.FRIDAY).firstOnOrAfter(d("2026-11-15")))
        assertEquals(d("2026-11-15"), Recurrence.Weekly(DayOfWeek.SUNDAY).firstOnOrAfter(d("2026-11-15")))
        assertEquals(d("2026-11-16"), Recurrence.Weekly(DayOfWeek.MONDAY).firstOnOrAfter(d("2026-11-15")))
    }

    @Test
    fun `yearly 29 February is 28 February in a common year`() {
        assertEquals(d("2027-02-28"), Recurrence.Yearly(2, 29).firstOnOrAfter(d("2026-11-15")))
        assertEquals(d("2028-02-29"), Recurrence.Yearly(2, 29).firstOnOrAfter(d("2027-03-01")))
        assertEquals(d("2027-03-01"), Recurrence.Yearly(3, 1).firstOnOrAfter(d("2026-11-15")))
        assertFailsWith<IllegalArgumentException> { Recurrence.Yearly(4, 31) }
        assertFailsWith<IllegalArgumentException> { Recurrence.Monthly(0) }
    }

    @Test
    fun `missed payments wait until handled and handling never moves back`() {
        val cleaning = RecurringSchedule(Recurrence.Weekly(DayOfWeek.FRIDAY), start = d("2026-11-01"))
        assertEquals(listOf(d("2026-11-06"), d("2026-11-13")), cleaning.due(d("2026-11-15")))
        val handled = cleaning.handledUpTo(d("2026-11-13"))
        assertEquals(emptyList(), handled.due(d("2026-11-15")))
        assertEquals(d("2026-11-20"), handled.nextUnhandled())
        assertEquals(d("2026-11-13"), handled.handledUpTo(d("2026-11-06")).handledThrough)
    }

    @Test
    fun `a forgotten weekly rule lists at most a capped number of days`() {
        val old = RecurringSchedule(Recurrence.Weekly(DayOfWeek.MONDAY), start = d("2020-01-01"))
        assertEquals(MAX_OCCURRENCES, old.due(d("2026-11-15")).size)
    }

    @Test
    fun `upcoming lists missed, today and the next 7 days in that order`() {
        fun tpl(name: String, r: Recurrence, handled: String? = null) = TransactionTemplate(
            name, name, TransactionType.EXPENSE, "galicia", ars,
            schedule = RecurringSchedule(r, d("2026-11-01"), handledThrough = handled?.let(::d)),
        )
        val today = d("2026-11-15")
        val list = upcomingPayments(
            listOf(
                tpl("Netflix", Recurrence.Monthly(18), "2026-10-18"),
                tpl("Аренда", Recurrence.Monthly(5), "2026-11-05"), // next — 5 December, not soon
                tpl("Свет", Recurrence.Monthly(15), "2026-10-15"),
                tpl("Уборка", Recurrence.Weekly(DayOfWeek.FRIDAY), "2026-10-30"),
                TransactionTemplate("Кофе", "Кофе", TransactionType.EXPENSE, "galicia", ars),
                tpl("Испанский", Recurrence.Weekly(DayOfWeek.TUESDAY), "2026-11-10"),
            ),
            today,
        )
        assertEquals(listOf("Уборка", "Свет", "Испанский", "Netflix"), list.map { it.template.name })
        assertEquals(listOf(PaymentState.MISSED, PaymentState.TODAY, PaymentState.SOON, PaymentState.SOON), list.map { it.state })
        assertEquals(listOf(d("2026-11-06"), d("2026-11-13")), list[0].missed)
    }

    @Test
    fun `only a template with an amount, not a transfer, posts by itself`() {
        val auto = RecurringSchedule(Recurrence.Monthly(5), d("2026-11-01"), PostMode.AUTO)
        assertFailsWith<IllegalArgumentException> {
            TransactionTemplate("x", "Свет", TransactionType.EXPENSE, "galicia", ars, schedule = auto)
        }
        assertFailsWith<IllegalArgumentException> {
            TransactionTemplate("x", "В наличные", TransactionType.TRANSFER, "galicia", ars, Money(100, ars), toAccountId = "cash", schedule = auto)
        }
    }
}

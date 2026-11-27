package app.coshell.core.domain.recurring

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.Recurrence
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecurringDetectorTest {
    private val ars = CurrencyCode("ARS")
    private val tz = TimeZone.UTC
    private val today = LocalDate(2026, 11, 27)

    private fun tx(id: String, date: LocalDate, amount: Long = 25_000_00, payee: String? = "Telecentro", category: String? = "internet") = Transaction(
        id = id, type = TransactionType.EXPENSE, accountId = "galicia", amount = Money(amount, ars),
        date = date.atStartOfDayIn(tz), categoryId = category, payee = payee,
    )

    private fun detect(vararg list: Transaction, templates: List<TransactionTemplate> = emptyList(), dismissed: Set<String> = emptySet()) =
        recurringCandidates(list.toList(), templates, dismissed, tz, today)

    private val monthly = arrayOf(
        tx("a", LocalDate(2026, 8, 5)), tx("b", LocalDate(2026, 9, 5), 25_500_00), tx("c", LocalDate(2026, 10, 6)), tx("d", LocalDate(2026, 11, 5), 26_000_00),
    )

    @Test
    fun `the same payee about a month apart is a monthly payment on its last day`() {
        val found = detect(*monthly).single()
        assertEquals(Recurrence.Monthly(5), found.recurrence)
        assertEquals("d", found.latest.id)
        assertEquals(Money(25_500_00, ars), found.amount)
        assertEquals(LocalDate(2026, 12, 5), found.next)
    }

    @Test
    fun `a weekly habit is found too`() {
        val weekly = (0..3).map { tx("w$it", LocalDate(2026, 11, 4 + it * 7), 3_000_00, payee = "Clases") }.toTypedArray()
        assertEquals(Recurrence.Weekly(DayOfWeek.WEDNESDAY), detect(*weekly).single().recurrence)
    }

    @Test
    fun `two times, scattered amounts, irregular gaps or a stopped habit are not payments`() {
        assertTrue(detect(monthly[2], monthly[3]).isEmpty())
        assertTrue(detect(tx("a", LocalDate(2026, 9, 5)), tx("b", LocalDate(2026, 10, 5), 40_000_00), tx("c", LocalDate(2026, 11, 5))).isEmpty())
        assertTrue(detect(tx("a", LocalDate(2026, 9, 5)), tx("b", LocalDate(2026, 9, 20)), tx("c", LocalDate(2026, 11, 5))).isEmpty())
        assertTrue(detect(tx("a", LocalDate(2026, 6, 5)), tx("b", LocalDate(2026, 7, 5)), tx("c", LocalDate(2026, 8, 5))).isEmpty())
    }

    @Test
    fun `a payment with a scheduled template or turned down is not offered again`() {
        val template = TransactionTemplate(
            id = "t", name = "Интернет", type = TransactionType.EXPENSE, accountId = "galicia", currency = ars, categoryId = "internet",
            schedule = RecurringSchedule(Recurrence.Monthly(5), LocalDate(2026, 12, 1)),
        )
        assertTrue(detect(*monthly, templates = listOf(template)).isEmpty())
        val key = detect(*monthly).single().key
        assertTrue(detect(*monthly, dismissed = setOf(key)).isEmpty())
    }
}

package app.coshell.core.domain.recurring

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

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
}

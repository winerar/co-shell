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

class PaymentListTest {
    private val today = LocalDate(2026, 11, 15) // Sunday
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")

    private fun rule(
        name: String,
        recurrence: Recurrence,
        amount: Money? = null,
        type: TransactionType = TransactionType.EXPENSE,
        handled: LocalDate? = null,
        start: LocalDate = LocalDate(2026, 1, 1),
    ) = TransactionTemplate(
        id = name,
        name = name,
        type = type,
        accountId = "a",
        currency = amount?.currency ?: ars,
        amount = amount,
        toAccountId = if (type == TransactionType.TRANSFER) "b" else null,
        schedule = RecurringSchedule(recurrence, start, PostMode.REMIND, handled),
    )

    @Test
    fun `list has every rule by next day then name, with missed and today marked`() {
        val templates = listOf(
            rule("Аренда", Recurrence.Monthly(5), handled = LocalDate(2026, 11, 5)),
            rule("Уборка", Recurrence.Weekly(DayOfWeek.FRIDAY), handled = LocalDate(2026, 10, 30)),
            rule("Свет", Recurrence.Monthly(15), handled = LocalDate(2026, 10, 15)),
            rule("Газ", Recurrence.Monthly(15), handled = LocalDate(2026, 10, 15)),
            TransactionTemplate("coffee", "Кофе", TransactionType.EXPENSE, "a", ars),
        )
        val list = scheduledPayments(templates, today)
        assertEquals(listOf("Уборка", "Газ", "Свет", "Аренда"), list.map { it.template.name })
        assertEquals(PaymentState.MISSED, list[0].state)
        assertEquals(2, list[0].missed) // 6 and 13 November
        assertEquals(PaymentState.TODAY, list[1].state)
        assertEquals(LocalDate(2026, 12, 5), list[3].date)
        assertEquals(PaymentState.SOON, list[3].state)
    }

    @Test
    fun `month ahead counts every payment until the end of the month, missed included`() {
        val templates = listOf(
            // missed 13 Nov + 20 and 27 Nov
            rule("Уборка", Recurrence.Weekly(DayOfWeek.FRIDAY), Money(30_000_00, ars), handled = LocalDate(2026, 11, 6)),
            rule("Зарплата", Recurrence.Monthly(28), Money(1_200_00, usd), TransactionType.INCOME, handled = LocalDate(2026, 10, 28)),
            rule("Свет", Recurrence.Monthly(20), handled = LocalDate(2026, 10, 20)),
            rule("Копилка", Recurrence.Monthly(25), Money(100_00, usd), TransactionType.TRANSFER, handled = LocalDate(2026, 10, 25)),
            rule("Аренда", Recurrence.Monthly(5), Money(650_00, usd), handled = LocalDate(2026, 11, 5)), // next in December
        )
        val ahead = paymentsToMonthEnd(templates, today)
        assertEquals(6, ahead.count)
        assertEquals(1, ahead.withoutAmount)
        assertEquals(
            listOf(Money(-30_000_00, ars), Money(-30_000_00, ars), Money(-30_000_00, ars), Money(1_200_00, usd)),
            ahead.amounts,
        )
    }

    @Test
    fun `month ahead is empty when everything is handled`() {
        val templates = listOf(rule("Аренда", Recurrence.Monthly(5), Money(650_00, usd), handled = LocalDate(2026, 11, 5)))
        assertEquals(MonthAhead(0, emptyList(), 0), paymentsToMonthEnd(templates, today))
    }
}

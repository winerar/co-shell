package app.coshell.core.domain.recurring

import app.coshell.core.model.Money
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * One rule on «Регулярные платежи» (#143, board `Rec143List`): its oldest payment not handled yet.
 * [state] — [PaymentState.SOON] for any later day (the list shows the date), [missed] — overdue payment days.
 */
data class ScheduledPayment(
    val template: TransactionTemplate,
    val date: LocalDate,
    val state: PaymentState,
    val missed: Int = 0,
)

/** Every template with «Повтор», by the next payment day, then by name: overdue ones come first by themselves. */
fun scheduledPayments(templates: List<TransactionTemplate>, today: LocalDate): List<ScheduledPayment> =
    templates.mapNotNull { template ->
        val schedule = template.schedule ?: return@mapNotNull null
        val next = schedule.nextUnhandled()
        val ahead = daysFrom(today, next)
        when {
            ahead < 0 -> ScheduledPayment(template, next, PaymentState.MISSED, schedule.due(today).count { it < today })
            ahead == 0 -> ScheduledPayment(template, next, PaymentState.TODAY)
            else -> ScheduledPayment(template, next, PaymentState.SOON)
        }
    }.sortedWith(compareBy<ScheduledPayment>({ it.date }, { it.template.name.lowercase() }))

/**
 * Payments left until the end of the month of today, missed ones included — the hero of «Регулярные платежи»
 * («До конца ноября · 9 платежей»). A weekly rule counts every week. [amounts] are signed: an expense negative,
 * an income positive; a transfer only moves money between accounts, so it is counted but not summed.
 * [withoutAmount] — payments whose amount is entered when posting («без «сумма при вводе» · 3»).
 */
data class MonthAhead(
    val count: Int,
    val amounts: List<Money>,
    val withoutAmount: Int,
)

fun paymentsToMonthEnd(templates: List<TransactionTemplate>, today: LocalDate): MonthAhead {
    val end = LocalDate(today.year, today.month, 1).plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
    var count = 0
    var withoutAmount = 0
    val amounts = mutableListOf<Money>()
    for (template in templates) {
        val schedule = template.schedule ?: continue
        val days = schedule.recurrence.between(schedule.nextUnhandled(), end).size
        if (days == 0) continue
        count += days
        if (template.type == TransactionType.TRANSFER) continue
        val amount = template.amount
        if (amount == null) {
            withoutAmount += days
        } else {
            repeat(days) { amounts += if (template.type == TransactionType.INCOME) amount else -amount }
        }
    }
    return MonthAhead(count, amounts, withoutAmount)
}

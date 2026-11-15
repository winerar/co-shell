package app.coshell.core.domain.recurring

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionTemplate
import kotlinx.datetime.LocalDate

/** Where a payment stands on «Обзоре» («Скоро · 7 дней», owner picked А for #143). */
enum class PaymentState { MISSED, TODAY, SOON }

/**
 * One row of «Скоро»: the oldest payment of [template] not handled yet. [missed] — overdue payment
 * days (before today), oldest first: «Уборка — пропущено 2» posts or skips them together.
 */
data class UpcomingPayment(
    val template: TransactionTemplate,
    val date: LocalDate,
    val state: PaymentState,
    val missed: List<LocalDate> = emptyList(),
)

/**
 * Regular payments due within [days] days from [today] (today included), plus every missed one:
 * missed first, then by date, then by name. Templates without «Повтор» are not listed.
 */
fun upcomingPayments(templates: List<TransactionTemplate>, today: LocalDate, days: Int = SOON_DAYS): List<UpcomingPayment> =
    templates.mapNotNull { template ->
        val schedule = template.schedule ?: return@mapNotNull null
        val next = schedule.nextUnhandled()
        val ahead = daysFrom(today, next)
        when {
            ahead < 0 -> UpcomingPayment(template, next, PaymentState.MISSED, schedule.due(today).filter { it < today })
            ahead == 0 -> UpcomingPayment(template, next, PaymentState.TODAY)
            ahead < days -> UpcomingPayment(template, next, PaymentState.SOON)
            else -> null
        }
    }.sortedWith(compareBy<UpcomingPayment>({ it.state.ordinal }, { it.date }, { it.template.name.lowercase() }))

/** «Скоро · 7 дней». */
const val SOON_DAYS = 7

/**
 * Templates with a payment today that still waits for «Провести» — the 9:00 reminder (#143, board
 * `Rec143Notify`). Call after [PostAutoPaymentsUseCase]: a «само» payment that posted is handled and
 * drops out, one that could not post (account archived) stays and is reminded like any other. [snoozed] —
 * templates whose reminder was put off till today with «Завтра».
 */
fun paymentsToRemind(
    templates: List<TransactionTemplate>,
    today: LocalDate,
    snoozed: Set<String> = emptySet(),
): List<TransactionTemplate> = templates.filter { template ->
    val due = template.schedule?.due(today).orEmpty()
    // «Завтра» on yesterday's reminder (#405): asked again today while the payment still waits.
    due.lastOrNull() == today || (template.id in snoozed && due.isNotEmpty())
}.sortedBy { it.name.lowercase() }

/**
 * «3 платежа сегодня · ≈ 96 USD» (#405): what the reminded payments come to in [main] at today's rates; template
 * amounts are sizes, so an income and an expense on one day add up (both are money to handle). Payments with «сумма при вводе»
 * and currencies without a rate are left out — hence «≈»; `null` — nothing to count.
 */
fun remindTotal(templates: List<TransactionTemplate>, converter: CurrencyConverter, main: CurrencyCode, today: LocalDate): Money? {
    val amounts = templates.mapNotNull { it.amount }
    if (amounts.isEmpty()) return null
    val total = converter.total(amounts.map { it to today }, main)
    return total.sum.takeIf { amounts.any { it.currency !in total.missing } }
}

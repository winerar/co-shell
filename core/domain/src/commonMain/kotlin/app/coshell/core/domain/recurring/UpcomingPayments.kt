package app.coshell.core.domain.recurring

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

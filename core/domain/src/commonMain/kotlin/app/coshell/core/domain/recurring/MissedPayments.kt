package app.coshell.core.domain.recurring

import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.model.TransactionTemplate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/**
 * «Уборка — пропущено 2» with «Провести оба» / «Пропустить» (#406, board `Rec143Edge`): missed payments pile up
 * instead of getting lost and are handled together.
 */
class MissedPaymentsUseCase(
    private val templates: TemplateRepository,
    private val post: PostPaymentUseCase,
    private val markHandled: MarkPaymentsHandledUseCase = MarkPaymentsHandledUseCase(templates),
) {
    /**
     * Posts every missed payment of [templateId] (before [today]) by the template's amount, oldest first. Stops at
     * the first one that cannot post (no amount, category archived): the rest stay missed and the sheet takes over.
     * Returns how many were posted.
     */
    suspend fun postAll(templateId: String, today: LocalDate, timeZone: TimeZone): Int {
        val days = missedDays(templateId, today)
        var posted = 0
        for (day in days) {
            post(templateId, day, timeZone) ?: break
            posted++
        }
        return posted
    }

    /** «Пропустить»: every missed payment of [templateId] is done without an operation; today's one still waits. */
    suspend fun skipAll(templateId: String, today: LocalDate): Boolean {
        val last = missedDays(templateId, today).lastOrNull() ?: return false
        return markHandled(templateId, last)
    }

    private suspend fun missedDays(templateId: String, today: LocalDate): List<LocalDate> =
        templates.getTemplate(templateId)?.missedDays(today).orEmpty()
}

/** Payment days before [today] that wait for «Провести» or «Пропустить», oldest first. */
fun TransactionTemplate.missedDays(today: LocalDate): List<LocalDate> = schedule?.due(today).orEmpty().filter { it < today }

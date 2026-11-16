package app.coshell.core.domain.recurring

import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.domain.templates.draftFrom
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionTemplate
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * «Провести» saved the operation, or «Пропустить» was tapped: every payment of the template on or
 * before [through] is done. `false` — the template is gone or has no «Повтор».
 */
class MarkPaymentsHandledUseCase(private val templates: TemplateRepository) {
    suspend operator fun invoke(templateId: String, through: LocalDate): Boolean {
        val template = templates.getTemplate(templateId) ?: return false
        val schedule = template.schedule ?: return false
        templates.saveTemplate(template.copy(schedule = schedule.handledUpTo(through)))
        return true
    }
}

/** A payment [PostAutoPaymentsUseCase] or [PostPaymentUseCase] posted: [template] gives the name for «Проведено: Netflix · …». */
data class PostedPayment(val template: TransactionTemplate, val transaction: Transaction)

/**
 * Posts due payments of «Проводить само» templates (#143) and returns them — for the «Проведено: …»
 * notification. A payment the template can no longer fill (account archived, category gone) is left
 * due: it shows as missed and waits for «Провести» by hand.
 *
 * The operation id is derived from the template and the day ([autoPaymentId]): when both phones post
 * the same payment before they sync, the second write lands on the same record instead of a duplicate.
 * A payment in another currency (#415) never posts by itself ([TransactionTemplate.postsByItself]): the bank's rate
 * differs from the official one, so the reminder opens the sheet and the user enters what was debited (owner, poll 3 «Д»).
 */
class PostAutoPaymentsUseCase(
    accounts: AccountRepository,
    categories: CategoryRepository,
    private val templates: TemplateRepository,
    save: SaveTransactionUseCase,
    private val clock: Clock = Clock.System,
) {
    private val poster = PaymentPoster(accounts, categories, save)

    suspend operator fun invoke(timeZone: TimeZone, authorMemberId: String? = null): List<PostedPayment> {
        val now = clock.now()
        val today = now.toLocalDateTime(timeZone).date
        val posted = mutableListOf<PostedPayment>()
        for (template in templates.allTemplates()) {
            if (!template.postsByItself) continue
            val schedule = template.schedule ?: continue
            var handled: LocalDate? = null
            for (day in schedule.due(today)) {
                val tx = poster.post(template, day, now, timeZone, authorMemberId) ?: break
                posted += PostedPayment(template, tx)
                handled = day
            }
            handled?.let { templates.saveTemplate(template.copy(schedule = schedule.handledUpTo(it))) }
        }
        return posted
    }
}

/**
 * «Провести 650 USD» on the reminder (#405): posts the payment of [day] by the template's amount without opening
 * the app, and marks it handled. `null` — nothing posted: the template is gone, has no «Повтор» or amount, or can
 * no longer be filled (account archived), or the amount is in another currency (#415: what was debited is entered
 * in the sheet) — the reminder stays, «Ввести сумму» opens the sheet.
 */
class PostPaymentUseCase(
    accounts: AccountRepository,
    categories: CategoryRepository,
    private val templates: TemplateRepository,
    save: SaveTransactionUseCase,
    private val clock: Clock = Clock.System,
) {
    private val poster = PaymentPoster(accounts, categories, save)

    suspend operator fun invoke(templateId: String, day: LocalDate, timeZone: TimeZone, authorMemberId: String? = null): PostedPayment? {
        val template = templates.getTemplate(templateId) ?: return null
        val schedule = template.schedule ?: return null
        if (template.amount == null) return null
        val tx = poster.post(template, day, clock.now(), timeZone, authorMemberId) ?: return null
        templates.saveTemplate(template.copy(schedule = schedule.handledUpTo(day)))
        return PostedPayment(template, tx)
    }
}

/**
 * One payment of a template as an operation dated its day (today — now, earlier — noon); `null` — cannot be filled
 * without the user: a gap (account archived, category gone) or a payment in another currency (#415), whose debited
 * amount only the user knows.
 */
private class PaymentPoster(
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val save: SaveTransactionUseCase,
) {
    suspend fun post(template: TransactionTemplate, day: LocalDate, now: Instant, timeZone: TimeZone, authorMemberId: String?): Transaction? {
        val draft = draftFrom(
            template, now,
            account = accounts.getAccount(template.accountId),
            target = null,
            category = template.categoryId?.let { categories.getCategory(it) },
        )
        if (template.isForeign || draft.gaps.isNotEmpty() || draft.accountId == null) return null
        val amount = draft.amount ?: return null
        val tx = Transaction(
            id = autoPaymentId(template.id, day),
            type = template.type,
            accountId = draft.accountId,
            amount = amount,
            date = if (day == now.toLocalDateTime(timeZone).date) now else day.atTime(NOON).toInstant(timeZone),
            categoryId = draft.categoryId,
            note = template.note,
            authorMemberId = authorMemberId,
        )
        return tx.takeIf { save(it) == SaveTransactionUseCase.Result.Saved }
    }

    private companion object {
        val NOON = LocalTime(12, 0)
    }
}

/** Same template + same day → same operation id on every phone. */
fun autoPaymentId(templateId: String, day: LocalDate): String = paymentIdPrefix(templateId) + day

/** The start of every payment id of the template ([autoPaymentId]). */
fun paymentIdPrefix(templateId: String): String = "rec-$templateId-"

private suspend fun TemplateRepository.allTemplates() = observeTemplates().first()

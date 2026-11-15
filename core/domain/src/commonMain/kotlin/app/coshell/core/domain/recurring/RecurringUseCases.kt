package app.coshell.core.domain.recurring

import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.domain.templates.draftFrom
import app.coshell.core.model.PostMode
import app.coshell.core.model.Transaction
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

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

/**
 * Posts due payments of «Проводить само» templates (#143) and returns them — for the «Проведено: …»
 * notification. A payment the template can no longer fill (account archived, category gone) is left
 * due: it shows as missed and waits for «Провести» by hand.
 *
 * The operation id is derived from the template and the day ([autoPaymentId]): when both phones post
 * the same payment before they sync, the second write lands on the same record instead of a duplicate.
 */
class PostAutoPaymentsUseCase(
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val templates: TemplateRepository,
    private val save: SaveTransactionUseCase,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(timeZone: TimeZone, authorMemberId: String? = null): List<Transaction> {
        val now = clock.now()
        val today = now.toLocalDateTime(timeZone).date
        val posted = mutableListOf<Transaction>()
        for (template in templates.allTemplates()) {
            val schedule = template.schedule?.takeIf { it.mode == PostMode.AUTO } ?: continue
            var handled: LocalDate? = null
            for (day in schedule.due(today)) {
                val draft = draftFrom(
                    template, now,
                    account = accounts.getAccount(template.accountId),
                    target = null,
                    category = template.categoryId?.let { categories.getCategory(it) },
                )
                val amount = draft.amount
                if (draft.gaps.isNotEmpty() || draft.accountId == null || amount == null) break
                val tx = Transaction(
                    id = autoPaymentId(template.id, day),
                    type = template.type,
                    accountId = draft.accountId,
                    amount = amount,
                    date = if (day == today) now else day.atTime(NOON).toInstant(timeZone),
                    categoryId = draft.categoryId,
                    note = template.note,
                    authorMemberId = authorMemberId,
                )
                if (save(tx) != SaveTransactionUseCase.Result.Saved) break
                posted += tx
                handled = day
            }
            handled?.let { templates.saveTemplate(template.copy(schedule = schedule.handledUpTo(it))) }
        }
        return posted
    }

    private companion object {
        val NOON = LocalTime(12, 0)
    }
}

/** Same template + same day → same operation id on every phone. */
fun autoPaymentId(templateId: String, day: LocalDate): String = "rec-$templateId-$day"

private suspend fun TemplateRepository.allTemplates() = observeTemplates().first()

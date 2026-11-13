package app.coshell.transactions.edit

import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Transaction
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * New transaction from the form, or [existing] updated; edits keep fields the form does not show (payee, import info…).
 * Labels: the chosen tags merged into [sourceLabels] — those of the edited or copied operation ([mergeLabels]).
 */
internal fun buildTransaction(
    existing: Transaction?,
    form: ValidForm,
    date: Instant,
    ids: IdGenerator,
    sourceLabels: List<String> = existing?.labels.orEmpty(),
): Transaction {
    val labels = mergeLabels(sourceLabels, form.tagIds)
    val base = existing ?: return Transaction(
        id = ids.newId(),
        type = form.type,
        accountId = form.account.id,
        amount = form.amount,
        date = date,
        toAccountId = form.toAccount?.id,
        toAmount = form.toAmount,
        categoryId = form.categoryId,
        note = form.note,
        originalAmount = form.originalAmount,
        labels = labels,
    )
    return base.copy(
        type = form.type,
        accountId = form.account.id,
        amount = form.amount,
        toAccountId = form.toAccount?.id,
        toAmount = form.toAmount,
        categoryId = form.categoryId,
        date = date,
        note = form.note,
        originalAmount = form.originalAmount,
        labels = labels,
    )
}

/** Keeps the time of day of [reference] (the edited transaction or now) on the chosen [date]. */
internal fun dateWithTime(reference: Instant, date: LocalDate, timeZone: TimeZone): Instant {
    val local = reference.toLocalDateTime(timeZone)
    if (local.date == date) return reference
    return date.atTime(local.time).toInstant(timeZone)
}

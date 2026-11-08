package app.coshell.transactions.edit

import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Transaction
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/** New transaction from the form, or [existing] updated; edits keep fields the form does not show (payee, labels, import info…). */
internal fun buildTransaction(existing: Transaction?, form: ValidForm, date: Instant, ids: IdGenerator): Transaction {
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
    )
}

/** Keeps the time of day of [reference] (the edited transaction or now) on the chosen [date]. */
internal fun dateWithTime(reference: Instant, date: LocalDate, timeZone: TimeZone): Instant {
    val local = reference.toLocalDateTime(timeZone)
    if (local.date == date) return reference
    return date.atTime(local.time).toInstant(timeZone)
}

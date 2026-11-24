package app.coshell.core.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

/** Who owes whom (#552): [LENT] — «Мне должны» (I lent), [BORROWED] — «Я должен» (I borrowed). */
enum class DebtDirection { LENT, BORROWED }

/** What happened to a debt: it was [ISSUE]d, a part was [RETURN]ed, the rest was [CLOSE]d (forgiven or settled outside the app). */
enum class DebtEventKind { ISSUE, RETURN, CLOSE }

/**
 * One line of a debt's history (#552): events are only ever added, so two phones adding returns at once never overwrite each other.
 * Every event of a debt has the same [debtId] and [direction]. [person], [dueDate] and [note] live on the [DebtEventKind.ISSUE] event.
 * ISSUE and RETURN move [accountId] (a loan out of it, a return into it; for a borrowed debt the other way round) but are neither
 * spending nor income: reports never see them. A [DebtEventKind.CLOSE] event has no account and no amount.
 */
data class DebtEvent(
    val id: String,
    val debtId: String,
    val kind: DebtEventKind,
    val direction: DebtDirection,
    val amount: Money,
    val date: Instant,
    val accountId: String? = null,
    val person: String = "",
    val dueDate: LocalDate? = null,
    val note: String = "",
) {
    init {
        when (kind) {
            DebtEventKind.ISSUE -> {
                require(amount.isPositive) { "A debt must be positive" }
                require(accountId != null) { "A debt moves an account" }
                require(person.isNotBlank()) { "A debt needs a person" }
            }
            DebtEventKind.RETURN -> {
                require(amount.isPositive) { "A return must be positive" }
                require(accountId != null) { "A return moves an account" }
            }
            DebtEventKind.CLOSE -> require(amount.amountMinor == 0L && accountId == null) { "Closing a debt moves no money" }
        }
    }
}

/** A debt as the screens show it: its events folded (#552). [left] is what is still to be returned; a [closed] debt is out of the active lists. */
data class Debt(
    val id: String,
    val direction: DebtDirection,
    val person: String,
    val total: Money,
    val returned: Money,
    val left: Money,
    val dueDate: LocalDate?,
    val note: String,
    val closed: Boolean,
    val issuedAt: Instant,
    val returns: List<DebtEvent>,
    val accountId: String,
)

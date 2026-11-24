package app.coshell.core.domain.debts

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Debt
import app.coshell.core.model.DebtDirection
import app.coshell.core.model.DebtEvent
import app.coshell.core.model.DebtEventKind
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Folds the events of debts into [Debt]s (#552) and answers the questions of the screens: totals, who is late. Pure. */
object DebtBook {
    /**
     * One [Debt] per debt id that has its ISSUE event (a return that arrived by sync before its issue waits). Newest issue first.
     * [Debt.left] never goes below zero; a debt is closed when nothing is left or a CLOSE event was added.
     */
    fun of(events: List<DebtEvent>): List<Debt> =
        events.groupBy { it.debtId }.mapNotNull { (id, list) ->
            val issue = list.firstOrNull { it.kind == DebtEventKind.ISSUE } ?: return@mapNotNull null
            val returns = list.filter { it.kind == DebtEventKind.RETURN }.sortedBy { it.date }
            val returned = returns.sumOf { it.amount.amountMinor }
            val left = (issue.amount.amountMinor - returned).coerceAtLeast(0L)
            val currency = issue.amount.currency
            Debt(
                id = id,
                direction = issue.direction,
                person = issue.person,
                total = issue.amount,
                returned = Money(returned.coerceAtMost(issue.amount.amountMinor), currency),
                left = Money(left, currency),
                dueDate = issue.dueDate,
                note = issue.note,
                closed = left == 0L || list.any { it.kind == DebtEventKind.CLOSE },
                issuedAt = issue.date,
                returns = returns,
                accountId = issue.accountId.orEmpty(),
            )
        }.sortedByDescending { it.issuedAt }

    fun active(debts: List<Debt>, direction: DebtDirection): List<Debt> = debts.filter { !it.closed && it.direction == direction }

    fun closed(debts: List<Debt>, direction: DebtDirection): List<Debt> = debts.filter { it.closed && it.direction == direction }

    /** What is still owed in each currency, by [direction], active debts only; the biggest sum first, [main] before the rest when it has one. */
    fun totals(debts: List<Debt>, direction: DebtDirection, main: CurrencyCode? = null): List<Money> =
        active(debts, direction).groupBy { it.left.currency }
            .map { (currency, list) -> Money(list.sumOf { it.left.amountMinor }, currency) }
            .sortedWith(compareByDescending<Money> { it.currency == main }.thenByDescending { it.amountMinor })

    /** The stage of a debt's return day: nothing to say, soon, today or late. */
    enum class Stage { NONE, SOON, TODAY, LATE }

    data class Due(val debt: Debt, val stage: Stage, val days: Int)

    /**
     * Active debts whose day is within [days] days or already past (#552, «плашка» and «Скоро»): late first, then by day.
     * A debt without a day, or a closed one, is never listed.
     */
    fun due(debts: List<Debt>, today: LocalDate, days: Int = REMIND_DAYS): List<Due> =
        debts.filter { !it.closed }.mapNotNull { debt ->
            val day = debt.dueDate ?: return@mapNotNull null
            val ahead = today.daysUntil(day)
            when {
                ahead < 0 -> Due(debt, Stage.LATE, -ahead)
                ahead == 0 -> Due(debt, Stage.TODAY, 0)
                ahead <= days -> Due(debt, Stage.SOON, ahead)
                else -> null
            }
        }.sortedWith(compareBy<Due>({ if (it.stage == Stage.LATE) 0 else 1 }, { it.debt.dueDate?.toEpochDays() ?: 0 }))

    /** «За 3 дня и в день срока» (poll 2026-11-23). */
    const val REMIND_DAYS = 3
}

package app.coshell.home.upcoming

import app.coshell.core.domain.debts.DebtBook
import app.coshell.core.model.DebtDirection
import app.coshell.core.model.MoneyFormat

/**
 * A debt whose return day is near, today or past (#552, board `HomeDebtSoon`): a line in «Скоро · 7 дней» next to the regular payments.
 * [days] — days ahead for [DebtBook.Stage.SOON], days late for [DebtBook.Stage.LATE]. Never red: the line only says it.
 */
data class DebtDueRow(
    val debtId: String,
    val person: String,
    val lent: Boolean,
    val stage: DebtBook.Stage,
    val days: Int,
    val left: String,
)

fun buildDebtRows(due: List<DebtBook.Due>, moneyFormat: MoneyFormat): List<DebtDueRow> = due.map {
    DebtDueRow(it.debt.id, it.debt.person, it.debt.direction == DebtDirection.LENT, it.stage, it.days, moneyFormat.format(it.debt.left))
}

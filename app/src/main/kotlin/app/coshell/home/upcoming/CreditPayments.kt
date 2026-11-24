package app.coshell.home.upcoming

import app.coshell.core.domain.credit.CreditCard
import app.coshell.core.domain.credit.CreditPaymentDue
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.LocalDate

/**
 * A credit card payment in «Скоро · 7 дней» (#686, board `HomeCreditSoon`): the card, the day and the minimum as a formatted sum.
 * «Провести» opens a transfer into the card for this sum.
 */
data class CreditPaymentRow(
    val accountId: String,
    val name: String,
    val color: Long?,
    val due: LocalDate,
    val daysLeft: Int,
    val minimum: String,
    /** The interest-free day (#634), or `null` when no grace period is set. */
    val graceUntil: LocalDate? = null,
)

fun buildCreditRows(due: List<CreditPaymentDue>, moneyFormat: MoneyFormat, today: LocalDate): List<CreditPaymentRow> = due.map {
    CreditPaymentRow(it.account.id, it.account.name, it.account.color, it.due, it.daysLeft, moneyFormat.format(it.minimum), CreditCard.graceUntil(it.terms, today))
}

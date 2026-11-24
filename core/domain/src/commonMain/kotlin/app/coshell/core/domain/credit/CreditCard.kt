package app.coshell.core.domain.credit

import app.coshell.core.model.CreditTerms
import app.coshell.core.model.MinPayment
import app.coshell.core.model.Money
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * What a credit card shows (#686): the [debt] (the balance when it is negative), what is [available] of the limit, and — while there is
 * a debt — the next [dueDate] and the [minPayment] due then. [limit] is repeated so the screen draws the meter from one object.
 */
data class CreditStatus(
    val limit: Money,
    val debt: Money,
    val available: Money,
    val dueDate: LocalDate?,
    val minPayment: Money?,
    /** The last day without interest (#634): the last statement plus the grace days, or the next one's when that has passed; `null` — no grace period set or no debt. */
    val graceUntil: LocalDate? = null,
)

object CreditCard {
    /** The status of a card with [terms] whose balance is [balance] on [today]. Money paid in advance (a positive balance) is no debt. */
    fun status(terms: CreditTerms, balance: Money, today: LocalDate): CreditStatus {
        val currency = balance.currency
        val limit = Money(terms.limitMinor, currency)
        val debt = Money(if (balance.amountMinor < 0) -balance.amountMinor else 0L, currency)
        val available = Money((terms.limitMinor - debt.amountMinor).coerceAtLeast(0L), currency)
        val has = debt.amountMinor > 0
        return CreditStatus(limit, debt, available, if (has) dueDate(terms.payDay, today) else null, if (has) minimum(terms.minPayment, debt) else null, if (has) graceUntil(terms, today) else null)
    }

    /** The last day without interest: the latest statement on or before [today] plus the grace days while that is still ahead, else the next statement's. */
    fun graceUntil(terms: CreditTerms, today: LocalDate): LocalDate? {
        val day = terms.statementDay ?: return null
        val grace = terms.graceDays ?: return null
        val next = dueDate(day, today)
        val last = if (next == today) today else previousDue(day, next)
        val fromLast = last.plus(DatePeriod(days = grace))
        return if (fromLast >= today) fromLast else next.plus(DatePeriod(days = grace))
    }

    /** The minimum payment of a [debt]: a share of it (at least one minor unit) or the fixed sum, never more than the debt itself. */
    fun minimum(rule: MinPayment, debt: Money): Money = when (rule) {
        is MinPayment.Percent -> Money(debt.scale(rule.basisPoints.toLong(), BASIS_POINTS).amountMinor.coerceAtLeast(1L), debt.currency)
        is MinPayment.Fixed -> Money(minOf(rule.minor, debt.amountMinor), debt.currency)
    }

    /** The next day of the month [payDay] on or after [today]; in a short month the day is its last one (the 31st → the 30th). */
    fun dueDate(payDay: Int, today: LocalDate): LocalDate {
        val thisMonth = inMonth(today.year, today.monthNumber, payDay)
        if (thisMonth >= today) return thisMonth
        val next = LocalDate(today.year, today.monthNumber, 1).plus(DatePeriod(months = 1))
        return inMonth(next.year, next.monthNumber, payDay)
    }

    /** The due date before [due]: where the billing cycle that ends with [due] began (payments made after it count for [due]). */
    fun previousDue(payDay: Int, due: LocalDate): LocalDate {
        val prev = LocalDate(due.year, due.monthNumber, 1).minus(DatePeriod(months = 1))
        return inMonth(prev.year, prev.monthNumber, payDay)
    }

    private fun inMonth(year: Int, month: Int, day: Int): LocalDate {
        val last = LocalDate(year, month, 1).plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).dayOfMonth
        return LocalDate(year, month, minOf(day, last))
    }

    private const val BASIS_POINTS = 10_000L
}

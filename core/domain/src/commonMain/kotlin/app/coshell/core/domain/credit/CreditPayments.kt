package app.coshell.core.domain.credit

import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CreditTerms
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

/** A credit card payment that waits in «Скоро · 7 дней» (#686): the [minimum] due on [due], [daysLeft] days from today (0 — today). */
data class CreditPaymentDue(val account: Account, val terms: CreditTerms, val due: LocalDate, val minimum: Money, val daysLeft: Int)

/**
 * Credit card payments due within [days] days from [today] (today included), by date then name. A card without a debt has nothing
 * to pay; a payment already made — transfers into the card since the previous due date ([paidSince] ignores older ones) reaching the
 * minimum — drops out, so «Скоро» goes quiet after a payment. [transfers] are any operations of the cards; only transfers into them count.
 */
fun creditPaymentsDue(
    accounts: List<AccountWithBalance>,
    transfers: List<Transaction>,
    today: LocalDate,
    timeZone: TimeZone,
    days: Int = SOON_CREDIT_DAYS,
): List<CreditPaymentDue> = accounts.mapNotNull { item ->
    val account = item.account
    val terms = account.credit ?: return@mapNotNull null
    if (account.archived) return@mapNotNull null
    val status = CreditCard.status(terms, item.balance, today)
    val due = status.dueDate ?: return@mapNotNull null
    val minimum = status.minPayment ?: return@mapNotNull null
    val ahead = today.daysUntil(due)
    if (ahead >= days) return@mapNotNull null
    val cycleStart = CreditCard.previousDue(terms.payDay, due)
    if (paidSince(account.id, transfers, cycleStart, minimum, timeZone)) return@mapNotNull null
    CreditPaymentDue(account, terms, due, minimum, ahead)
}.sortedWith(compareBy<CreditPaymentDue>({ it.due }, { it.account.name.lowercase() }))

/** The same window as the regular payments of «Скоро» (`SOON_DAYS`). */
const val SOON_CREDIT_DAYS = 7

/** Transfers into [accountId] dated after [cycleStart] add up to at least [minimum] (the payment of this cycle is made). */
internal fun paidSince(accountId: String, transfers: List<Transaction>, cycleStart: LocalDate, minimum: Money, timeZone: TimeZone): Boolean {
    val paid = transfers
        .filter { it.type == TransactionType.TRANSFER && it.toAccountId == accountId && it.date.toLocalDateTime(timeZone).date > cycleStart }
        .sumOf { (it.toAmount ?: it.amount).amountMinor }
    return paid >= minimum.amountMinor
}

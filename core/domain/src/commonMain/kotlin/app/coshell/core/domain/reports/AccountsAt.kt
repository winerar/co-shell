package app.coshell.core.domain.reports

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/**
 * One account on a point of «Капитал» (#373, board `Rp189NwDrill`): [balance] in its currency at the point's day,
 * [converted] — in the report currency at that day's rate (`null` — no rate), [change] — how [converted] moved since the
 * month before, at each day's rate (`null` — no earlier point or no rate on one of them).
 */
data class AccountWorth(val account: Account, val balance: Money, val converted: Money?, val change: Money?)

/**
 * The accounts that make up capital at [asOf] — counted into the total, as [netWorth] counts them — and how each moved
 * since [previousAsOf]. An account empty on both days is left out; one emptied during the month stays (its change
 * matters).
 */
fun accountsAt(
    accounts: List<Account>,
    days: List<BalanceDay>,
    book: RateBook,
    currency: CurrencyCode,
    asOf: LocalDate,
    previousAsOf: LocalDate?,
): List<AccountWorth> {
    val byAccount = days.groupBy { it.accountId }
    return accounts.filter { !it.excludedFromTotal }.mapNotNull { account ->
        val own = byAccount[account.id].orEmpty()
        val balance = balanceAt(account, own, asOf)
        val before = previousAsOf?.let { balanceAt(account, own, it) }
        if (balance.amountMinor == 0L && (before == null || before.amountMinor == 0L)) return@mapNotNull null
        val converted = convertAt(balance, book, currency, asOf)
        val previous = before?.let { convertAt(it, book, currency, previousAsOf) }
        AccountWorth(account, balance, converted, if (converted != null && previous != null) converted - previous else null)
    }
}

/** [account]'s balance at the end of [day]: the opening balance plus every change of [days] up to it. */
internal fun balanceAt(account: Account, days: List<BalanceDay>, day: LocalDate): Money =
    Money(account.initialBalanceMinor + days.filter { it.day <= day }.sumOf { it.deltaMinor }, account.currency)

private fun convertAt(money: Money, book: RateBook, currency: CurrencyCode, day: LocalDate): Money? =
    if (money.amountMinor == 0L) Money.zero(currency) else book.find(money.currency, currency, day)?.rate?.convert(money, currency)

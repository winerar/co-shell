package app.coshell.core.domain.reports

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate

/**
 * The sum of one day's operations of one account and category (`ReportDao.observeAccountDays`, #242): the same as
 * [ReportDay] but by the account the money left, not by who added it. [amount] is in the account's currency.
 */
data class ReportAccountDay(
    val day: LocalDate,
    val accountId: String,
    val categoryId: String,
    val type: TransactionType,
    val amount: Money,
)

/** What one account paid: [native] in its own currency, [converted] in the report currency at the rate of each day. */
data class AccountPaid(val accountId: String, val native: Money, val converted: Money, val percent: Int)

/** What was paid in one currency (all accounts of it): [native] and [converted], [percent] of the spending. */
data class CurrencyPaid(val currency: CurrencyCode, val native: Money, val converted: Money, val percent: Int)

/**
 * «Чем платили» (#242): net spending of a period by account and by the currency it was paid in. [total] is in the
 * report currency; shares are whole percents of [total] and add up to 100. [missing] — currencies left out for lack of a rate.
 */
data class PaidWith(
    val total: Money,
    val byAccount: List<AccountPaid>,
    val byCurrency: List<CurrencyPaid>,
    val missing: Set<CurrencyCode> = emptySet(),
) {
    val isEmpty: Boolean get() = byAccount.isEmpty()
}

/**
 * Spending of [days] by account and currency, in [currency] at the rate of each day — the rules of #187: an expense
 * spends, an income in an expense category is a refund and reduces it, other income and «Корректировка баланса»
 * are not spending (transfers never reach [days]). Accounts and currencies that spent nothing net are left out;
 * largest first, ties — by id/code, so the order is stable.
 */
fun paidWith(days: List<ReportAccountDay>, categories: List<Category>, book: RateBook, currency: CurrencyCode): PaidWith {
    val types = categories.associate { it.id to it.type }
    val native = linkedMapOf<String, Money>()
    val converted = linkedMapOf<String, Money>()
    val missing = linkedSetOf<CurrencyCode>()
    for (d in days) {
        if (SystemCategories.isAdjustment(d.categoryId)) continue
        val signed = when (d.type) {
            TransactionType.EXPENSE -> d.amount
            TransactionType.INCOME -> if (types[d.categoryId] == CategoryType.EXPENSE) -d.amount else continue
            TransactionType.TRANSFER -> continue
        }
        val rate = book.find(signed.currency, currency, d.day)?.rate
        if (rate == null) { missing += signed.currency; continue }
        native[d.accountId] = (native[d.accountId] ?: Money.zero(signed.currency)) + signed
        converted[d.accountId] = (converted[d.accountId] ?: Money.zero(currency)) + rate.convert(signed, currency)
    }
    val rows = native.keys.map { Triple(it, native.getValue(it), converted.getValue(it)) }
        .filter { it.third.isPositive }
        .sortedWith(compareByDescending<Triple<String, Money, Money>> { it.third.amountMinor }.thenBy { it.first })
    val accountShares = percentShares(rows.map { it.third.amountMinor })
    val byAccount = rows.mapIndexed { i, (id, n, c) -> AccountPaid(id, n, c, accountShares[i]) }

    val perCurrency = rows.groupBy { it.second.currency }.map { (code, list) ->
        Triple(code, list.fold(Money.zero(code)) { a, r -> a + r.second }, list.fold(Money.zero(currency)) { a, r -> a + r.third })
    }.sortedWith(compareByDescending<Triple<CurrencyCode, Money, Money>> { it.third.amountMinor }.thenBy { it.first.value })
    val currencyShares = percentShares(perCurrency.map { it.third.amountMinor })
    val byCurrency = perCurrency.mapIndexed { i, (code, n, c) -> CurrencyPaid(code, n, c, currencyShares[i]) }

    return PaidWith(rows.fold(Money.zero(currency)) { a, r -> a + r.third }, byAccount, byCurrency, missing)
}

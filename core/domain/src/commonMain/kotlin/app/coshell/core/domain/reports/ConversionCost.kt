package app.coshell.core.domain.reports

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate

/**
 * A purchase paid in another currency than its price (#786): [paid] left the account (its currency), [price] is «Сумма покупки».
 * [type] — [TransactionType.EXPENSE], or [TransactionType.INCOME] for a refund, which counts back.
 */
data class ForeignPurchase(
    val id: String,
    val day: LocalDate,
    val accountId: String,
    val type: TransactionType,
    val paid: Money,
    val price: Money,
)

/** [paid] against [official] — the price at the official rate of the day, in the same currency; [extraBp] — the difference in 0,01 %. */
data class PaidAgainstOfficial(val paid: Money, val official: Money) {
    /** (paid − official) / official in hundredths of a percent: 180 — «хуже на 1,8 %», negative — the rate was better. */
    val extraBp: Long get() = if (official.amountMinor == 0L) 0L else (paid.abs() - official.abs()).scale(BP, official.abs().amountMinor).amountMinor
}

/** One currency pair: everything paid in [paidCurrency] for prices in [priceCurrency] ([price] — their sum). */
data class PairCost(val priceCurrency: CurrencyCode, val price: Money, val cost: PaidAgainstOfficial) {
    val paidCurrency: CurrencyCode get() = cost.paid.currency
}

/** One account (card) and how its purchases compare with the official rate. */
data class AccountCost(val accountId: String, val cost: PaidAgainstOfficial)

/** One purchase with its official price. */
data class PurchaseCost(val purchase: ForeignPurchase, val cost: PaidAgainstOfficial)

/**
 * «Оплата в другой валюте» (#786, poll 2026-11-27 «А», board `Smart786Card`): how much paying in another currency cost over the
 * official rate of each day. [total] is in the report currency: positive — paid more than the official rate, negative — less.
 * [pairs] biggest first, [accounts] worst first, [purchases] newest first. [missing] — currencies without a rate: those purchases are left out.
 */
data class ConversionCost(
    val total: Money,
    val pairs: List<PairCost>,
    val accounts: List<AccountCost>,
    val purchases: List<PurchaseCost>,
    val missing: Set<CurrencyCode> = emptySet(),
)

/**
 * Counts [ConversionCost] of [purchases] by [book] in [currency]; `null` when there is nothing to count. Each purchase: its price
 * at the official rate of its day in the paid currency, and the difference with what was paid, at the rate of the day in [currency].
 */
fun conversionCost(purchases: List<ForeignPurchase>, book: RateBook, currency: CurrencyCode): ConversionCost? {
    val converter = CurrencyConverter(book)
    val missing = mutableSetOf<CurrencyCode>()
    val counted = purchases.mapNotNull { p ->
        val sign = if (p.type == TransactionType.INCOME) -1L else 1L
        val official = converter.convert(p.price, p.paid.currency, p.day)?.money
        val extra = official?.let { converter.convert(p.paid - it, currency, p.day)?.money }
        if (official == null || extra == null) {
            // Name the currency that has no rate, not the one of a pair that merely cannot be crossed.
            val lacking = listOf(p.price, p.paid).filter { converter.convert(it, currency, p.day) == null }.map { it.currency }
            missing += lacking.ifEmpty { listOf(p.price.currency, p.paid.currency) }
            return@mapNotNull null
        }
        Counted(p, PaidAgainstOfficial(p.paid.times(sign), official.times(sign)), extra.times(sign))
    }
    if (counted.isEmpty()) return null
    val total = counted.fold(Money.zero(currency)) { acc, c -> acc + c.extra }
    val pairs = counted.groupBy { it.purchase.price.currency to it.purchase.paid.currency }.map { (key, list) ->
        PairCost(key.first, list.sumOf(key.first) { it.purchase.price.times(it.sign) }, list.cost(key.second))
    }.sortedByDescending { it.cost.paid.amountMinor }
    val accounts = counted.groupBy { it.purchase.accountId }.map { (id, list) -> AccountCost(id, list.cost(list.first().purchase.paid.currency)) }
        .sortedWith(compareByDescending<AccountCost> { it.cost.extraBp }.thenBy { it.accountId })
    return ConversionCost(
        total = total,
        pairs = pairs,
        accounts = accounts,
        purchases = counted.sortedWith(compareByDescending<Counted> { it.purchase.day }.thenBy { it.purchase.id }).map { PurchaseCost(it.purchase, it.cost) },
        missing = missing,
    )
}

private data class Counted(val purchase: ForeignPurchase, val cost: PaidAgainstOfficial, val extra: Money) {
    val sign: Long get() = if (purchase.type == TransactionType.INCOME) -1L else 1L
}

private fun List<Counted>.cost(paidCurrency: CurrencyCode) =
    PaidAgainstOfficial(sumOf(paidCurrency) { it.cost.paid }, sumOf(paidCurrency) { it.cost.official })

private fun List<Counted>.sumOf(currency: CurrencyCode, pick: (Counted) -> Money): Money = fold(Money.zero(currency)) { acc, c -> acc + pick(c) }

private fun Money.times(sign: Long): Money = if (sign < 0) -this else this

private const val BP = 10_000L

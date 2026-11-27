package app.coshell.reports

import app.coshell.core.domain.reports.ConversionCost
import app.coshell.core.domain.reports.PaidAgainstOfficial
import app.coshell.core.model.Account
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Rate
import app.coshell.ui.format.formatRate
import kotlinx.datetime.LocalDate
import kotlin.math.abs

/**
 * «Оплата в другой валюте» (#786, board `Smart786Card`): what paying in another currency cost over the official rate.
 * Amounts carry their codes (several currencies meet here); [total] is in the report currency, without a sign.
 */
data class Conversion(
    val total: String,
    /** The rate was better than the official one on the whole: «вышло выгоднее на …». */
    val better: Boolean,
    /** The biggest currency pair: «Покупки в ARS с карты USDT». */
    val pair: ConversionPair,
    val accounts: List<ConversionAccount>,
    /** For «Подробнее»: every purchase, newest first. */
    val purchases: List<ConversionPurchase>,
    val missing: List<String> = emptyList(),
)

/** [rate] — «1 USDT = 997 ARS», [officialRate] — the same direction, number only; [percent] — «1,8» without a sign. */
data class ConversionPair(
    val priceCurrency: String,
    val paidCurrency: String,
    val price: String,
    val paid: String,
    val official: String,
    val rate: String,
    val officialRate: String,
    val percent: String,
    val worse: Boolean,
)

/** One card: [name] `null` — an account deleted on another phone. */
data class ConversionAccount(val accountId: String, val name: String?, val currency: String, val percent: String, val worse: Boolean)

data class ConversionPurchase(
    val id: String,
    val day: LocalDate,
    val account: String?,
    val price: String,
    val paid: String,
    val rate: String,
    val percent: String,
    val worse: Boolean,
)

/** Pure: the card of [cost]; `null` — nothing paid in another currency in the period. */
internal fun buildConversion(cost: ConversionCost?, accounts: List<Account>, format: MoneyFormat): Conversion? {
    cost ?: return null
    val pair = cost.pairs.first()
    val names = accounts.associate { it.id to it.name }
    return Conversion(
        total = format.format(cost.total.abs()),
        better = cost.total.isNegative,
        pair = ConversionPair(
            priceCurrency = pair.priceCurrency.value,
            paidCurrency = pair.paidCurrency.value,
            price = format.format(pair.price),
            paid = format.format(pair.cost.paid),
            official = format.format(pair.cost.official),
            rate = rateText(pair.cost.paid, pair.price),
            officialRate = numberOfRate(pair.cost.official, pair.price, pair.cost.paid, pair.price),
            percent = percentText(pair.cost, format),
            worse = pair.cost.extraBp > 0,
        ),
        accounts = cost.accounts.map {
            ConversionAccount(it.accountId, names[it.accountId], it.cost.paid.currency.value, percentText(it.cost, format), it.cost.extraBp > 0)
        },
        purchases = cost.purchases.map {
            ConversionPurchase(
                id = it.purchase.id,
                day = it.purchase.day,
                account = names[it.purchase.accountId],
                price = format.format(it.purchase.price),
                paid = format.format(it.purchase.paid),
                rate = rateText(it.purchase.paid, it.purchase.price),
                percent = percentText(it.cost, format),
                worse = it.cost.extraBp > 0,
            )
        },
        missing = cost.missing.map { it.value }.sorted(),
    )
}

/** «1 USDT = 997 ARS»: the direction in which the number is at least 1, as the receipt line of the entry sheet. */
internal fun rateText(paid: Money, price: Money): String {
    if (paid.amountMinor == 0L || price.amountMinor == 0L) return ""
    val rate = Rate.between(paid.abs(), price.abs())
    return if (rate >= Rate.ONE) "1 ${paid.currency.value} = ${formatRate(rate)} ${price.currency.value}"
    else "1 ${price.currency.value} = ${formatRate(rate.inverse())} ${paid.currency.value}"
}

/** The official rate as a number, in the direction [rateText] chose for [paid] and [price]. */
private fun numberOfRate(official: Money, officialPrice: Money, paid: Money, price: Money): String {
    if (listOf(official, officialPrice, paid, price).any { it.amountMinor == 0L }) return ""
    val rate = Rate.between(official.abs(), officialPrice.abs())
    return formatRate(if (Rate.between(paid.abs(), price.abs()) >= Rate.ONE) rate else rate.inverse())
}

/** «1,8» from 180 hundredths of a percent; the sign is told by words. */
internal fun percentText(cost: PaidAgainstOfficial, format: MoneyFormat): String {
    val bp = abs(cost.extraBp)
    val tenths = (bp + TENTH / 2) / TENTH
    return "${tenths / TEN}${format.decimalSeparator}${tenths % TEN}"
}

private const val TENTH = 10L
private const val TEN = 10L

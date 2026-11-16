package app.coshell.recurring.list

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.recurring.paymentsToMonthEnd
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionTemplate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * The hero «До конца ноября» of «Регулярные платежи»: payments left this month ([paymentsToMonthEnd]), their net in
 * [main] at the rates of [today] (future days have no rate yet). Currencies without a rate are left out and named.
 */
internal fun monthAheadUi(
    templates: List<TransactionTemplate>,
    rates: List<ExchangeRate>,
    main: CurrencyCode,
    today: LocalDate,
    moneyFormat: MoneyFormat,
): MonthAheadUi {
    val ahead = paymentsToMonthEnd(templates, today)
    val base = MonthAheadUi(YearMonth(today.year, today.month), ahead.count, withoutAmount = ahead.withoutAmount)
    if (ahead.amounts.isEmpty()) return base
    val sum = CurrencyConverter(RateBook.of(rates)).total(ahead.amounts.map { it to today }, main)
    val known = ahead.amounts.any { it.currency !in sum.missing }
    return base.copy(total = if (known) signed(sum.sum, moneyFormat) else null, missing = sum.missing.map { it.value })
}

/** «−410 USD» / «+1 200 USD» / «0 USD»: the sign of the net, as on the rows. */
private fun signed(money: Money, moneyFormat: MoneyFormat): String = when {
    money.amountMinor < 0 -> "−" + moneyFormat.format(-money)
    money.amountMinor > 0 -> "+" + moneyFormat.format(money)
    else -> moneyFormat.format(money)
}

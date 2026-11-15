package app.coshell.transactions.list

import app.coshell.core.designsystem.components.PeekLine
import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.rates.amountIn
import app.coshell.core.model.Currencies
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.LocalDate

/**
 * The bubble of a held amount (#318, `Fb318A`): [lines] — the amount in each report currency at [date] (the
 * operation's day); a line with `amount == null` has no rate. Shown over the row [rowId] while the finger holds.
 */
data class AmountPeek(val rowId: String, val lines: List<PeekLine>, val date: LocalDate)

/**
 * The bubble for [money] of row [rowId] at [date], or `null` when there is nothing to add — every report
 * currency is the amount's own. [missing] is the text of a line without a rate («нет курса»).
 */
internal fun RateBook.amountPeek(
    rowId: String,
    money: Money,
    date: LocalDate,
    currencies: List<CurrencyCode>,
    moneyFormat: MoneyFormat,
    missing: String,
): AmountPeek? {
    val lines = CurrencyConverter(this).amountIn(money, currencies, date)
    if (lines.isEmpty()) return null
    return AmountPeek(
        rowId = rowId,
        date = date,
        lines = lines.map { line ->
            val amount = line.money?.let { moneyFormat.formatNumber(it.amountMinor, Currencies.info(it.currency).fractionDigits) }
            PeekLine(amount, line.currency.value, missing)
        },
    )
}

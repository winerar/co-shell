package app.coshell.converter

import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.rates.amountIn
import app.coshell.core.domain.settings.ConverterPick
import app.coshell.core.model.Currencies
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.MoneyParser
import kotlinx.datetime.LocalDate

/** The keypad sum in [currency]: terms added up, an unfinished «12,» read as «12»; nothing typed is zero. */
internal fun keypadMoney(text: String, currency: CurrencyCode): Money =
    text.split(AmountKeys.PLUS)
        .mapNotNull { MoneyParser.parseOrNull(it.trimEnd(','), currency) }
        .fold(Money.zero(currency)) { sum, term -> sum + term }

/**
 * [amount] in each of [currencies] at [date] as bare numbers, a zero fraction hidden («101 500», «100,10»);
 * a currency without a rate maps to null, [amount]'s own currency to the amount itself.
 */
internal fun RateBook.converted(amount: Money, currencies: List<CurrencyCode>, date: LocalDate, format: MoneyFormat): Map<CurrencyCode, String?> {
    val numbers = format.copy(trimZeroFraction = true)
    val others = CurrencyConverter(this).amountIn(amount, currencies, date).associate { it.currency to it.money }
    return currencies.associateWith { code ->
        val money = if (code == amount.currency) amount else others[code]
        money?.let { numbers.formatNumber(it.amountMinor, Currencies.info(it.currency).fractionDigits) }
    }
}

/**
 * The pair to open with: the remembered one while both currencies are still in [options]; otherwise the main currency [base] →
 * the first other one that has a rate ([rated]; none has — the first other). The pair holds two different currencies whenever
 * [options] has two.
 */
internal fun resolvePair(options: List<CurrencyCode>, base: CurrencyCode, pick: ConverterPick, rated: Set<CurrencyCode>): Pair<CurrencyCode, CurrencyCode> {
    val from = pick.from?.takeIf { it in options } ?: base
    val others = options.filter { it != from }
    val to = pick.to?.takeIf { it in others } ?: others.firstOrNull { it in rated } ?: others.firstOrNull() ?: from
    return from to to
}

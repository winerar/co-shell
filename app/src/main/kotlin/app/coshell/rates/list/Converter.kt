package app.coshell.rates.list

import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.rates.amountIn
import app.coshell.core.model.Currencies
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.MoneyParser
import kotlinx.datetime.LocalDate

/**
 * The converter on top of «Курсы» (#319, board `Fb319A`): [text] — what was typed on the own keypad
 * («100», «32000 + 1500»), [from] — its currency, `null` — the main report currency.
 */
data class ConverterInput(val text: String = DEFAULT_AMOUNT, val from: CurrencyCode? = null) {
    fun type(key: KeypadKey, currency: CurrencyCode): ConverterInput =
        copy(text = AmountKeys.apply(text, key, Currencies.info(currency).fractionDigits))

    /** Another currency keeps the number when it fits (JPY has no fraction: «12,5» → «12»). */
    fun from(currency: CurrencyCode): ConverterInput {
        val digits = Currencies.info(currency).fractionDigits
        val kept = if (digits == 0) text.split(AmountKeys.PLUS).joinToString(AmountKeys.PLUS) { it.substringBefore(',') } else text
        return ConverterInput(kept, currency)
    }

    companion object {
        const val DEFAULT_AMOUNT = "100"
    }
}

/** What the screen shows: the typed amount, its currency, every currency it can switch to, and the result per currency. */
data class ConverterUi(
    /** «32 000 + 1 500», grouped as on the keypad. */
    val amountText: String = ConverterInput.DEFAULT_AMOUNT,
    val from: String = "USD",
    val options: List<String> = emptyList(),
    /** The amount in the main report currency when [from] is another one («≈ 31,53 USD»), else null. */
    val inMain: String? = null,
)

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

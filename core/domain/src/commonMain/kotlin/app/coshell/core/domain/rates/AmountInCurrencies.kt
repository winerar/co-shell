package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/** One line of «≈ 31,53 USD»: [money] is `null` when no rate to [currency] is known (UI says «нет курса»). */
data class AmountIn(val currency: CurrencyCode, val money: Money?)

/**
 * [money] in each of [currencies] at [date] (#318 long-press hint, #319 converter on «Курсы»).
 * Keeps the order of [currencies] (the main report currency first) and skips the amount's own currency,
 * which is already on screen. Operations convert at their own date, balances and the converter at today.
 */
fun CurrencyConverter.amountIn(money: Money, currencies: List<CurrencyCode>, date: LocalDate): List<AmountIn> =
    currencies.distinct()
        .filter { it != money.currency }
        .map { AmountIn(it, convert(money, it, date)?.money) }

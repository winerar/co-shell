package app.coshell.currencies.list

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.rates.balanceTotals
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.LocalDate

/**
 * «Общий баланс · так будет на «Обзоре»» (board `ReportCurrencies`): the total balance in every report
 * currency, main first — the same sums as the hero of «Обзора» ([balanceTotals] at the rates of [today]).
 * Empty when there are no accounts yet: nothing to preview.
 */
internal fun balancePreview(
    accounts: List<AccountWithBalance>,
    rates: List<ExchangeRate>,
    currencies: List<CurrencyCode>,
    today: LocalDate,
    moneyFormat: MoneyFormat,
): List<String> {
    val balances = accounts.filterNot { it.account.excludedFromTotal }.map { it.balance }
    if (balances.isEmpty()) return emptyList()
    return RateBook.of(rates).balanceTotals(balances, currencies, today).map { moneyFormat.format(it.sum) }
}

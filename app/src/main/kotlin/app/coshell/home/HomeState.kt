package app.coshell.home

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.rates.balanceTotals
import app.coshell.core.domain.rates.dailyExpenses
import app.coshell.core.domain.rates.expenseTotals
import app.coshell.core.domain.rates.incomeTotals
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.core.model.isBalanceAdjustment
import app.coshell.core.model.sum
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

/**
 * Everything on «Обзор» except the latest operations: totals (balances at the rates of [today]),
 * this month's expenses and incomes (each at the rate of its day), the daily bars and the warnings.
 */
internal fun buildHomeState(
    accounts: List<AccountWithBalance>,
    monthTransactions: List<Transaction>,
    rates: List<ExchangeRate>,
    currencies: List<CurrencyCode>,
    today: LocalDate,
    timeZone: TimeZone,
    moneyFormat: MoneyFormat,
): HomeUiState {
    val book = RateBook.of(rates)
    val balances = accounts.filterNot { it.account.excludedFromTotal }.map { it.balance }
    val totals = book.balanceTotals(balances, currencies, today)
    val spending = book.expenseTotals(monthTransactions, currencies, timeZone)
    val income = book.incomeTotals(monthTransactions, currencies, timeZone)
    val byCurrency = balances.groupBy { it.currency }.map { (currency, items) -> moneyFormat.format(items.sum(currency)) }
    val rateDate = totals.mapNotNull { it.rateDate }.minOrNull()
    val missing = totals.firstOrNull()?.missing.orEmpty().map { it.value }
    val spendingMissing = spending.firstOrNull()?.missing.orEmpty().map { it.value }
    return HomeUiState(
        isLoading = false,
        totals = totals.map { moneyFormat.format(it.sum) },
        rateDate = rateDate,
        missing = missing,
        byCurrency = byCurrency.takeIf { it.size > 1 }.orEmpty(),
        spending = HomeUiState.MonthSpending(
            month = LocalDate(today.year, today.month, 1),
            totals = spending.map { moneyFormat.format(it.sum) },
            missing = spendingMissing,
            converted = monthTransactions.any { t -> t.type == TransactionType.EXPENSE && !t.isBalanceAdjustment && currencies.any { it != t.amount.currency } },
            daily = currencies.firstOrNull()
                ?.let { main -> book.dailyExpenses(monthTransactions, main, today, timeZone).map { it.amountMinor } }
                .orEmpty(),
            today = today.day - 1,
        ),
        income = HomeUiState.MonthTotals(
            totals = income.map { moneyFormat.format(it.sum) },
            missing = income.firstOrNull()?.missing.orEmpty().map { it.value },
        ),
        attention = HomeUiState.Attention(
            missingRates = (missing + spendingMissing).distinct(),
            staleRatesSince = rateDate?.takeIf { it < today.minus(DatePeriod(days = HomeUiState.STALE_RATE_DAYS)) },
            uncategorized = monthTransactions.count { it.type != TransactionType.TRANSFER && it.categoryId == null },
        ),
        accounts = accounts.map { (account, balance) ->
            HomeUiState.AccountRow(
                account.id, account.name, moneyFormat.format(balance), balance.isNegative, account.excludedFromTotal,
                account.type, account.color,
            )
        },
    )
}

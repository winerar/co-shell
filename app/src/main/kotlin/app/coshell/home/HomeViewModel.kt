package app.coshell.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.rates.balanceTotals
import app.coshell.core.domain.rates.expenseTotals
import app.coshell.core.domain.rates.monthBounds
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.core.model.sum
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * "Overview": total of all live accounts in the report currencies at today's rates, this month's
 * expenses in the same currencies (each at the rate of its day), and the account list.
 * The month is fixed when the screen subscribes; reopening the app after midnight on the 1st picks up the new one.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    accounts: AccountRepository,
    transactions: TransactionRepository,
    rates: ExchangeRateRepository,
    reportCurrencies: ReportCurrencySettings,
    private val moneyFormat: MoneyFormat,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    private val today = clock.todayIn(timeZone)

    val state: StateFlow<HomeUiState> = combine(
        accounts.observeAccountsWithBalance(),
        transactions.observeTransactions(monthBounds(today, timeZone).let { (from, to) -> TransactionFilter(from = from, to = to) }),
        rates.observeRates(),
        reportCurrencies.currencies,
    ) { list, monthItems, all, currencies -> buildState(list, monthItems, all, currencies, today) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(isLoading = true))

    internal fun buildState(
        accounts: List<AccountWithBalance>,
        monthTransactions: List<Transaction>,
        rates: List<ExchangeRate>,
        currencies: List<CurrencyCode>,
        today: LocalDate,
    ): HomeUiState {
        val book = RateBook(rates)
        val balances = accounts.filterNot { it.account.excludedFromTotal }.map { it.balance }
        val totals = book.balanceTotals(balances, currencies, today)
        val spending = book.expenseTotals(monthTransactions, currencies, timeZone)
        val byCurrency = balances.groupBy { it.currency }
            .map { (currency, items) -> moneyFormat.format(items.sum(currency)) }
        return HomeUiState(
            isLoading = false,
            totals = totals.map { moneyFormat.format(it.sum) },
            rateDate = totals.mapNotNull { it.rateDate }.minOrNull(),
            missing = totals.firstOrNull()?.missing.orEmpty().map { it.value },
            byCurrency = byCurrency.takeIf { it.size > 1 }.orEmpty(),
            spending = HomeUiState.MonthSpending(
                month = LocalDate(today.year, today.month, 1),
                totals = spending.map { moneyFormat.format(it.sum) },
                missing = spending.firstOrNull()?.missing.orEmpty().map { it.value },
                converted = monthTransactions.any { t -> t.type == TransactionType.EXPENSE && currencies.any { it != t.amount.currency } },
            ),
            accounts = accounts.map { (account, balance) ->
                HomeUiState.AccountRow(
                    account.id, account.name, moneyFormat.format(balance), balance.isNegative, account.excludedFromTotal,
                    account.type, account.color,
                )
            },
        )
    }
}

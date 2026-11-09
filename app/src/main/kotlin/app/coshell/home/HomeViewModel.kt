package app.coshell.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.rates.balanceTotals
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.MoneyFormat
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

/** "Overview": total of all live accounts in the report currencies at today's rates, and the account list. */
@HiltViewModel
class HomeViewModel @Inject constructor(
    accounts: AccountRepository,
    rates: ExchangeRateRepository,
    reportCurrencies: ReportCurrencySettings,
    private val moneyFormat: MoneyFormat,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        accounts.observeAccountsWithBalance(),
        rates.observeRates(),
        reportCurrencies.currencies,
    ) { list, all, currencies -> buildState(list, all, currencies, clock.todayIn(timeZone)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(isLoading = true))

    internal fun buildState(
        accounts: List<AccountWithBalance>,
        rates: List<ExchangeRate>,
        currencies: List<CurrencyCode>,
        today: LocalDate,
    ): HomeUiState {
        val balances = accounts.map { it.balance }
        val totals = RateBook(rates).balanceTotals(balances, currencies, today)
        val byCurrency = balances.groupBy { it.currency }
            .map { (currency, items) -> moneyFormat.format(items.sum(currency)) }
        return HomeUiState(
            isLoading = false,
            totals = totals.map { moneyFormat.format(it.sum) },
            rateDate = totals.mapNotNull { it.rateDate }.minOrNull(),
            missing = totals.firstOrNull()?.missing.orEmpty().map { it.value },
            byCurrency = byCurrency.takeIf { it.size > 1 }.orEmpty(),
            accounts = accounts.map { (account, balance) ->
                HomeUiState.AccountRow(account.id, account.name, moneyFormat.format(balance), balance.isNegative)
            },
        )
    }
}

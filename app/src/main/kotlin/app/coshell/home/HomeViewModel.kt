package app.coshell.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.monthBounds
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.transactions.list.TransactionsUiState
import app.coshell.transactions.list.buildTransactionDays
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * «Обзор»: total of all live accounts in the report currencies at today's rates, this month's expenses
 * and incomes (each at the rate of its day), what needs a look, the accounts and the latest operations.
 * Sums and warnings are built in [buildHomeState]. The month is fixed when the screen subscribes;
 * reopening the app after midnight on the 1st picks up the new one.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    accounts: AccountRepository,
    transactions: TransactionRepository,
    rates: ExchangeRateRepository,
    reportCurrencies: ReportCurrencySettings,
    categories: CategoryRepository,
    otherMembers: OtherMembers,
    private val moneyFormat: MoneyFormat,
    clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    private val today = clock.todayIn(timeZone)

    private val money = combine(
        accounts.observeAccountsWithBalance(),
        transactions.observeTransactions(monthBounds(today, timeZone).let { (from, to) -> TransactionFilter(from = from, to = to) }),
        rates.observeRates(),
        reportCurrencies.currencies,
    ) { list, monthItems, all, currencies -> buildState(list, monthItems, all, currencies, today) }

    /** The newest operations, formatted by the same rules as the Transactions tab. */
    private val recent: Flow<List<TransactionsUiState.Row>> = combine(
        transactions.observeTransactions(TransactionFilter(limit = HomeUiState.RECENT_COUNT)),
        accounts.observeAccounts(includeArchived = true),
        categories.observeCategories(includeArchived = true),
        otherMembers.names(),
    ) { items, accountList, categoryList, authors ->
        buildTransactionDays(
            transactions = items,
            accountsById = accountList.associateBy { it.id },
            categoriesById = categoryList.associateBy { it.id },
            moneyFormat = moneyFormat,
            timeZone = timeZone,
            today = today,
            authors = authors,
        ).flatMap { it.rows }.take(HomeUiState.RECENT_COUNT)
    }

    val state: StateFlow<HomeUiState> = combine(money, recent) { home, rows -> home.copy(recent = rows) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(isLoading = true))

    internal fun buildState(
        accounts: List<AccountWithBalance>,
        monthTransactions: List<Transaction>,
        rates: List<ExchangeRate>,
        currencies: List<CurrencyCode>,
        today: LocalDate,
    ): HomeUiState = buildHomeState(accounts, monthTransactions, rates, currencies, today, timeZone, moneyFormat)
}

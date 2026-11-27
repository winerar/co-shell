package app.coshell.reports.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.reports.ReportPeriodKind
import app.coshell.core.domain.reports.ReportRepository
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MoneyFormat
import app.coshell.navigation.ReportCategoryDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn

/**
 * «Из чего сложилось» (#188): one category of the report the user tapped — its period, currency and comparison come in
 * the route. Live: an operation edited from here shows at once. Only this category's operations of the period are
 * read; the numbers come from day sums (`ReportDao`), as on the tab. Pure parts — `ReportCategoryBoard.kt`.
 */
@HiltViewModel
class ReportCategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    reports: ReportRepository,
    transactions: TransactionRepository,
    categories: CategoryRepository,
    accounts: AccountRepository,
    rates: ExchangeRateRepository,
    otherMembers: OtherMembers,
    moneyFormat: MoneyFormat,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {

    private val route = RouteArgs(savedStateHandle)
    private val today = clock.todayIn(timeZone)
    private val format = moneyFormat.copy(trimZeroFraction = true)

    /** Where the drill is counted; tests run it in place. Read once, on first [state]. */
    internal var work: CoroutineContext = Dispatchers.Default

    /** The route's period at first; a tap on a column of «По месяцам» shows that month (#238). */
    private val period = MutableStateFlow(route.period)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val data = period.flatMapLatest { period ->
        val chartEnd = trendChartEnd(period, route.startDay, today)
        val (from, to) = drillRange(period, route.startDay, chartEnd)
        val operations = transactions.observeTransactions(
            TransactionFilter(from = period.start.atStartOfDayIn(timeZone), to = period.endExclusive.atStartOfDayIn(timeZone)),
        )
        combine(
            reports.observeDays(from, to),
            categories.observeCategories(includeArchived = true),
            rates.observeRates(),
            combine(operations, accounts.observeAccounts(includeArchived = true), otherMembers.names(), ::Triple),
        ) { days, categoryList, rateList, (operationList, accountList, authors) ->
            buildCategoryDrill(
                DrillInput(
                    categoryId = route.categoryId,
                    ledger = ReportLedger(days, categoryList, RateBook.of(rateList), CurrencyCode(route.currency)),
                    categories = categoryList,
                    period = period,
                    startDay = route.startDay,
                    compare = route.compare,
                    operations = operationList,
                    accounts = accountList,
                    authors = authors,
                    timeZone = timeZone,
                    today = today,
                    chartEnd = chartEnd,
                ),
                format,
                moneyFormat,
            )
        }
    }

    val state: StateFlow<ReportCategoryUiState> by lazy {
        data.flowOn(work).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            ReportCategoryUiState(isLoading = true, period = route.period, currency = route.currency),
        )
    }

    /** A tap on a column of «По месяцам»: the same category in [month]. */
    fun onMonth(month: YearMonth) {
        period.value = ReportPeriod.month(month, route.startDay)
    }

    /** The route's arguments; keys are the property names of [ReportCategoryDestination]. */
    private class RouteArgs(handle: SavedStateHandle) {
        val categoryId: String = checkNotNull(handle["categoryId"])
        val startDay: Int = checkNotNull(handle["startDay"])
        val currency: String = checkNotNull(handle["currency"])
        val compare: Boolean = checkNotNull(handle["compare"])
        val period = ReportPeriod(
            kind = ReportPeriodKind.valueOf(checkNotNull(handle["kind"])),
            start = LocalDate.parse(checkNotNull<String>(handle["start"])),
            endExclusive = LocalDate.parse(checkNotNull<String>(handle["end"])),
            firstMonth = YearMonth(checkNotNull<Int>(handle["firstYear"]), checkNotNull<Int>(handle["firstMonth"])),
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/** The route to the drill of [categoryId] in the report the user is looking at. */
fun reportCategoryDestination(categoryId: String, period: ReportPeriod, startDay: Int, currency: String, compare: Boolean) =
    ReportCategoryDestination(
        categoryId = categoryId,
        kind = period.kind.name,
        start = period.start.toString(),
        end = period.endExclusive.toString(),
        firstYear = period.firstMonth.year,
        firstMonth = period.firstMonth.month.ordinal + 1,
        startDay = startDay,
        currency = currency,
        compare = compare,
    )

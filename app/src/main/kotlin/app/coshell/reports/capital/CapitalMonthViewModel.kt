package app.coshell.reports.capital

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.NET_WORTH_MONTHS
import app.coshell.core.domain.reports.ReportRepository
import app.coshell.core.domain.reports.reportMonths
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MoneyFormat
import app.coshell.navigation.CapitalMonthDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn

/**
 * «Капитал · октябрь» (#373): a tap on a point of «Капитал» opens the accounts at that month end. Balances come from
 * the day sums of balance changes (`ReportDao`), the same as the card counts, so the numbers agree. Pure part —
 * `CapitalMonthBoard.kt`.
 */
@HiltViewModel
class CapitalMonthViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    reports: ReportRepository,
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
    private val periods = reportMonths(route.last, NET_WORTH_MONTHS, route.startDay)

    /** Where the screen is counted; tests run it in place. Read once, on first [state]. */
    internal var work: CoroutineContext = Dispatchers.Default

    private val view = MutableStateFlow(CapitalView(route.month))

    private val data = combine(
        accounts.observeAccounts(includeArchived = true),
        reports.observeBalanceDays(),
        rates.observeRates(),
        otherMembers.names(),
        view,
    ) { accountList, days, rateList, others, v ->
        val input = CapitalInput(accountList, days, RateBook.of(rateList), CurrencyCode(route.currency), periods, today, others)
        buildCapitalMonth(input, v, format, format)
    }

    val state: StateFlow<CapitalMonthUiState> by lazy {
        data.flowOn(work).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            CapitalMonthUiState(isLoading = true, currency = route.currency, month = route.month),
        )
    }

    /** A tap on the line: another month end. */
    fun onMonth(month: YearMonth) = view.update { it.copy(month = month) }

    fun onQuery(query: String) = view.update { it.copy(query = query) }

    fun onOrder(order: CapitalOrder) = view.update { it.copy(order = order) }

    fun onToggleGroup(key: String) = view.update { it.copy(flipped = if (key in it.flipped) it.flipped - key else it.flipped + key) }

    /** The route's arguments; keys are the property names of [CapitalMonthDestination]. */
    private class RouteArgs(handle: SavedStateHandle) {
        val month = YearMonth(checkNotNull<Int>(handle["year"]), checkNotNull<Int>(handle["month"]))
        val last = YearMonth(checkNotNull<Int>(handle["lastYear"]), checkNotNull<Int>(handle["lastMonth"]))
        val startDay: Int = checkNotNull(handle["startDay"])
        val currency: String = checkNotNull(handle["currency"])
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

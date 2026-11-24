package app.coshell.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.tags.TagRepository
import app.coshell.transactions.list.buildTransactionDays
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import app.coshell.core.domain.budgets.budgetPeriodOf
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.settings.InMemoryScreenOptions
import app.coshell.core.domain.settings.ReportsStart
import app.coshell.core.domain.settings.ScreenOptionsStore
import app.coshell.core.domain.reports.ReportRepository
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MoneyFormat
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn

/**
 * «Отчёты» (#140): the chosen period (the current family month at first) and currency (the main totals currency at
 * first), the day sums of the period, the one before and the months of «По месяцам» — added up by SQLite
 * (`ReportDao`), so a year of reports never loads the operations. The choice lives while the tab does.
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val reports: ReportRepository,
    categories: CategoryRepository,
    accounts: AccountRepository,
    rates: ExchangeRateRepository,
    otherMembers: OtherMembers,
    transactions: TransactionRepository,
    tags: TagRepository,
    reportCurrencies: ReportCurrencySettings,
    monthStart: MonthStartSettings,
    private val moneyFormat: MoneyFormat,
    clock: Clock,
    private val timeZone: TimeZone,
    screenOptions: ScreenOptionsStore = InMemoryScreenOptions(),
) : ViewModel() {

    private val today = clock.todayIn(timeZone)
    private val format = moneyFormat.copy(trimZeroFraction = true)

    /** What the user picked; `null` period — the current month, `null` currency — the main one. */
    private val choice = MutableStateFlow(Choice())

    /** Where reports are counted; tests run it in place. Read once, on first [state]. */
    internal var work: CoroutineContext = Dispatchers.Default

    private val settings = combine(monthStart.monthStartDay, reportCurrencies.currencies, choice, screenOptions.options) { startDay, currencies, chosen, options ->
        // Nothing picked yet: the period «Настройки → Экраны → Отчёты» says (the current month, quarter or year).
        val period = chosen.period ?: budgetPeriodOf(today, startDay).let { now ->
            when (options.reportsStart) {
                ReportsStart.MONTH -> ReportPeriod.of(now)
                ReportsStart.QUARTER -> ReportPeriod.quarter(now.month, startDay)
                ReportsStart.YEAR -> ReportPeriod.year(now.month.year, startDay)
            }
        }
        val currency = chosen.currency?.takeIf { it in currencies } ?: currencies.first()
        Settings(period, startDay, currency, currencies, chosen.compare ?: options.reportsCompare)
    }.distinctUntilChanged()

    /** Accounts and their balance changes by day — «Капитал» (#189); the same for every period. */
    private val balances = combine(accounts.observeAccounts(includeArchived = true), reports.observeBalanceDays(), ::Pair)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val data = settings.flatMapLatest { s ->
        val (from, to) = s.period.loadRange(s.startDay, today)
        combine(
            reports.observeDays(from, to),
            categories.observeCategories(includeArchived = true),
            rates.observeRates(),
            otherMembers.names(),
            balances,
        ) { days, categoryList, rateList, others, (accountList, balanceDays) ->
            val book = RateBook.of(rateList)
            val input = ReportInput(
                ledger = ReportLedger(days, categoryList, book, s.currency),
                categories = categoryList,
                period = s.period,
                startDay = s.startDay,
                today = today,
                compare = s.compare,
            )
            ReportsUiState(
                period = s.period,
                startDay = s.startDay,
                today = today,
                currency = s.currency.value,
                currencies = s.currencies.map { it.value },
                compare = s.compare,
                spending = buildSpending(input, format),
                months = buildMonths(input, format),
                income = buildIncome(input, format),
                members = buildMembers(input, others, format),
                capital = buildCapital(input, accountList, balanceDays, book, format),
                calendar = buildCalendar(input, format),
            )
        }
    }

    /** The tapped day of «Календарь трат» (#553); dropped when the month changes. */
    private val selectedDay = MutableStateFlow<LocalDate?>(null)

    /** Operations of the tapped day, rows as in «Операции». */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dayOps = selectedDay.flatMapLatest { date ->
        if (date == null) return@flatMapLatest flowOf(null)
        val from = date.atStartOfDayIn(timeZone)
        val to = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
        combine(
            transactions.observeTransactions(TransactionFilter(from = from, to = to)),
            accounts.observeAccounts(includeArchived = true),
            categories.observeCategories(includeArchived = true),
            otherMembers.names(),
            tags.observeTags(),
        ) { list, accountList, categoryList, authors, tagList ->
            buildTransactionDays(
                transactions = list,
                accountsById = accountList.associateBy { it.id },
                categoriesById = categoryList.associateBy { it.id },
                moneyFormat = moneyFormat,
                timeZone = timeZone,
                today = today,
                authors = authors,
                tagsById = tagList.associateBy { it.id },
            ).firstOrNull()
        }
    }

    val state: StateFlow<ReportsUiState> by lazy {
        combine(data, selectedDay, dayOps) { base, picked, ops ->
            // The tapped day counts only while its month is the one shown.
            val shown = picked?.takeIf { day -> base.calendar?.cells?.any { it.date == day } == true }
            base.copy(selectedDay = shown, dayOps = ops.takeIf { shown != null })
        }.flowOn(work).stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ReportsUiState(isLoading = true))
    }

    /** ‹ and ›: the period of the same kind before or after. */
    fun onShift(by: Int) {
        val shown = state.value.period ?: return
        choice.value = choice.value.copy(period = shown.shift(by, state.value.startDay))
    }

    /** A tap on a day of «Календарь трат»: its operations show under the grid; the same day again hides them. */
    fun onDay(day: LocalDate) {
        selectedDay.value = day.takeIf { it != selectedDay.value }
    }

    /** «Показать» in «Период и валюта». */
    fun onApply(period: ReportPeriod, currency: String, compare: Boolean) {
        choice.value = Choice(period, CurrencyCode(currency), compare)
    }

    /** A tap on a column of «По месяцам»: that month. */
    fun onMonth(month: YearMonth) {
        choice.value = choice.value.copy(period = ReportPeriod.month(month, state.value.startDay))
    }

    fun onCurrency(currency: String) {
        choice.value = choice.value.copy(currency = CurrencyCode(currency))
    }

    private data class Choice(val period: ReportPeriod? = null, val currency: CurrencyCode? = null, val compare: Boolean? = null)

    private data class Settings(
        val period: ReportPeriod,
        val startDay: Int,
        val currency: CurrencyCode,
        val currencies: List<CurrencyCode>,
        val compare: Boolean,
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

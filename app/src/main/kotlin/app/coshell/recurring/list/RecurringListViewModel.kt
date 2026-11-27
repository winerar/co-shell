package app.coshell.recurring.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import app.coshell.core.domain.budgets.budgetPeriodOf
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.recurring.SHARE_MONTHS
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.ReportRepository
import app.coshell.core.domain.reports.monthlySeries
import app.coshell.core.domain.reports.reportMonths
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.navigation.RecurringListDestination
import app.coshell.recurring.fixed.buildFixed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.recurring.ScheduledPayment
import app.coshell.core.domain.recurring.activePayments
import app.coshell.core.domain.recurring.pausedPayments
import app.coshell.core.domain.recurring.scheduledPayments
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.model.MoneyFormat
import app.coshell.templates.TemplateItem
import app.coshell.templates.toItem
import app.coshell.ui.offMain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * «Регулярные платежи» (#143, board `Rec143List`): every template with «Повтор» by its next payment, the hero
 * «До конца ноября» in the main report currency at today's rates, search and chips. Paused payments (account
 * archived, #406) get a message and stay out of the hero. The day is fixed when the screen opens, like «Обзор».
 */
@HiltViewModel
class RecurringListViewModel @Inject constructor(
    templates: TemplateRepository,
    accounts: AccountRepository,
    categories: CategoryRepository,
    rates: ExchangeRateRepository,
    reportCurrencies: ReportCurrencySettings,
    private val moneyFormat: MoneyFormat,
    clock: Clock,
    timeZone: TimeZone,
    reports: ReportRepository,
    monthStart: MonthStartSettings,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val today = clock.todayIn(timeZone)
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(PaymentFilter.ALL)
    /** «До конца месяца · За год» (#771); «Все ›» of «Постоянные траты» in «Отчётах» opens on the year. */
    private val mode = MutableStateFlow(
        if (savedStateHandle.get<Boolean>(RecurringListDestination.ARG_YEAR) == true) RecurringMode.YEAR else RecurringMode.MONTH,
    )
    private val format = moneyFormat.copy(trimZeroFraction = true)

    /** Spending of the last months for the share of «За год»: the current family month and [SHARE_MONTHS] before it, by day. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val spending = monthStart.monthStartDay.flatMapLatest { startDay ->
        val months = reportMonths(budgetPeriodOf(today, startDay).month, SHARE_MONTHS + 1, startDay)
        reports.observeDays(months.first().start, months.last().endExclusive).map { days -> months to days }
    }

    /** «За год»: the repeating expenses a year in the main report currency (#771). */
    private val fixed = combine(
        templates.observeTemplates(),
        accounts.observeAccounts(includeArchived = true),
        categories.observeCategories(includeArchived = true),
        combine(rates.observeRates(), reportCurrencies.currencies, ::Pair),
        spending,
    ) { list, accountList, categoryList, (rateList, currencies), (months, days) ->
        val book = RateBook.of(rateList)
        val main = currencies.firstOrNull() ?: ReportCurrencySettings.DEFAULT.first()
        val series = monthlySeries(ReportLedger(days, categoryList, book, main), months)
        buildFixed(list, accountList.associateBy { it.id }, categoryList.associateBy { it.id }, book, main, today, series, format)
    }

    private val content = combine(
        templates.observeTemplates(),
        accounts.observeAccounts(includeArchived = true),
        categories.observeCategories(includeArchived = true),
        rates.observeRates(),
        reportCurrencies.currencies,
    ) { list, accountList, categoryList, rateList, currencies ->
        val accountsById = accountList.associateBy { it.id }
        val categoriesById = categoryList.associateBy { it.id }
        val withRule = list.filter { it.schedule != null }
        Content(
            payments = scheduledPayments(withRule, today),
            paused = pausedPayments(withRule, accountsById).map { PausedUi(it.template.id, it.template.name, it.accountName) },
            items = withRule.associate { it.id to it.toItem(accountsById, categoriesById, moneyFormat) },
            ahead = monthAheadUi(activePayments(withRule, accountsById), rateList, currencies.firstOrNull() ?: ReportCurrencySettings.DEFAULT.first(), today, moneyFormat),
        )
    }

    val state: StateFlow<RecurringListUiState> = combine(content, query, filter, mode, fixed) { c, q, f, m, y ->
        recurringList(c.payments, c.items, c.ahead, q, f, c.paused).copy(mode = m, fixed = y)
    }
        .offMain()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), RecurringListUiState(isLoading = true))

    fun onQueryChange(value: String) { query.value = value }

    fun onFilterChange(value: PaymentFilter) { filter.value = value }

    fun onModeChange(value: RecurringMode) { mode.value = value }

    private data class Content(
        val payments: List<ScheduledPayment>,
        val items: Map<String, TemplateItem>,
        val ahead: MonthAheadUi,
        val paused: List<PausedUi>,
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

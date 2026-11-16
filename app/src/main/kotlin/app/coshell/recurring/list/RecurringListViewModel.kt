package app.coshell.recurring.list

import androidx.lifecycle.ViewModel
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
) : ViewModel() {

    private val today = clock.todayIn(timeZone)
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(PaymentFilter.ALL)

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

    val state: StateFlow<RecurringListUiState> = combine(content, query, filter) { c, q, f -> recurringList(c.payments, c.items, c.ahead, q, f, c.paused) }
        .offMain()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), RecurringListUiState(isLoading = true))

    fun onQueryChange(value: String) { query.value = value }

    fun onFilterChange(value: PaymentFilter) { filter.value = value }

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

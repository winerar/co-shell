package app.coshell.budgets

import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.budgets.BudgetCalculator
import app.coshell.core.domain.budgets.BudgetLedger
import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.budgets.BudgetRepository
import app.coshell.core.domain.budgets.budgetCategoryIds
import app.coshell.core.domain.budgets.budgetOperations
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportRepository
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Transaction
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atStartOfDayIn

/**
 * Everything the budget screens compute from (list `budgets/list`, editor `budgets/edit`, envelope `budgets/detail`):
 * limits, categories with archived ones, spending, rates, the main report currency and the month start day.
 * Spending comes as day sums of the whole history (rollover walks back through earlier months), added up by
 * SQLite (#262) — the operations themselves are read only for one envelope and one period ([observeEnvelope]).
 */
class BudgetSource @Inject constructor(
    private val budgets: BudgetRepository,
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
    private val reports: ReportRepository,
    private val rates: ExchangeRateRepository,
    private val reportCurrencies: ReportCurrencySettings,
    private val monthStart: MonthStartSettings,
    private val timeZone: TimeZone,
) {
    fun observe(): Flow<Snapshot> = combine(
        combine(budgets.observeBudgets(), categories.observeCategories(includeArchived = true), ::Pair),
        reports.observeAllDays(),
        rates.observeRates(),
        reportCurrencies.currencies,
        monthStart.monthStartDay,
    ) { (limits, categoryList), days, rateList, currencies, startDay ->
        Snapshot(limits, categoryList, days, rateList, currencies.first(), startDay)
    }

    /** [observe] with the operations behind the limit on [categoryId] in [month] (the envelope screen), newest first. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeEnvelope(categoryId: String, month: YearMonth): Flow<Envelope> {
        val periodOperations = monthStart.monthStartDay
            .distinctUntilChanged()
            .flatMapLatest { startDay ->
                val period = budgetPeriod(month, startDay)
                transactions.observeTransactions(
                    TransactionFilter(from = period.start.atStartOfDayIn(timeZone), to = period.endExclusive.atStartOfDayIn(timeZone)),
                )
            }
        return combine(observe(), periodOperations) { snapshot, operations ->
            val ids = budgetCategoryIds(categoryId, snapshot.categories)
            Envelope(snapshot, budgetOperations(operations, ids, snapshot.period(month), timeZone))
        }
    }

    class Envelope(val snapshot: Snapshot, val operations: List<Transaction>)

    /** One moment of the data; the ledger and the calculator are built on demand. */
    class Snapshot(
        val budgets: List<Budget>,
        val categories: List<Category>,
        private val days: List<ReportDay>,
        private val rates: List<ExchangeRate>,
        val mainCurrency: CurrencyCode,
        val startDay: Int,
    ) {
        val book: RateBook by lazy { RateBook.of(rates) }
        val ledger: BudgetLedger by lazy { BudgetLedger(days, book) }

        fun calculator(): BudgetCalculator = BudgetCalculator(budgets, categories, ledger, book, startDay)

        fun period(month: YearMonth): BudgetPeriod = budgetPeriod(month, startDay)
    }
}

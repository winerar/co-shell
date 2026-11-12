package app.coshell.budgets

import app.coshell.core.domain.CategoryRepository
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
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.core.domain.settings.ReportCurrencySettings
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Transaction
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth

/**
 * Everything the budget screens compute from (list `budgets/list`, editor `budgets/edit`): limits,
 * categories with archived ones, operations, rates, the main report currency and the month start day.
 * All operations are read: rollover walks back through earlier months, and a family has a few thousand.
 */
class BudgetSource @Inject constructor(
    private val budgets: BudgetRepository,
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
    private val rates: ExchangeRateRepository,
    private val reportCurrencies: ReportCurrencySettings,
    private val monthStart: MonthStartSettings,
    private val timeZone: TimeZone,
) {
    fun observe(): Flow<Snapshot> = combine(
        combine(budgets.observeBudgets(), categories.observeCategories(includeArchived = true), ::Pair),
        transactions.observeTransactions(),
        rates.observeRates(),
        reportCurrencies.currencies,
        monthStart.monthStartDay,
    ) { (limits, categoryList), operations, rateList, currencies, startDay ->
        Snapshot(limits, categoryList, operations, rateList, currencies.first(), startDay, timeZone)
    }

    /** One moment of the data; the ledger and the calculator are built on demand (they index all operations). */
    class Snapshot(
        val budgets: List<Budget>,
        val categories: List<Category>,
        private val transactions: List<Transaction>,
        private val rates: List<ExchangeRate>,
        val mainCurrency: CurrencyCode,
        val startDay: Int,
        private val timeZone: TimeZone,
    ) {
        val book: RateBook by lazy { RateBook.of(rates) }
        val ledger: BudgetLedger by lazy { BudgetLedger(transactions, book, timeZone) }

        fun calculator(): BudgetCalculator = BudgetCalculator(budgets, categories, ledger, book, startDay)

        fun period(month: YearMonth): BudgetPeriod = budgetPeriod(month, startDay)

        /** The operations behind the limit on [categoryId] in [month] (the envelope screen), newest first. */
        fun operations(categoryId: String, month: YearMonth): List<Transaction> =
            budgetOperations(transactions, budgetCategoryIds(categoryId, categories), period(month), timeZone)
    }
}

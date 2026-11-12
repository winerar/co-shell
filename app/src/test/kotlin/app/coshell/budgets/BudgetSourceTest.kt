package app.coshell.budgets

import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.budgets.BudgetRepository
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeReportCurrencySettings
import app.coshell.testing.FakeReportRepository
import app.coshell.testing.FakeTransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atStartOfDayIn
import org.junit.Assert.assertEquals
import org.junit.Test

/** #262: the budget screens count from day sums; operations are read for one envelope and one period only. */
class BudgetSourceTest {
    private val tz = TimeZone.UTC
    private val ars = CurrencyCode("ARS")
    private val transactions = FakeTransactionRepository()
    private val categories = FakeCategoryRepository()
    private val monthStart = object : MonthStartSettings {
        override val monthStartDay = MutableStateFlow(25)
        override suspend fun setMonthStartDay(day: Int) { monthStartDay.value = day }
    }
    private val noBudgets = object : BudgetRepository {
        override fun observeBudgets() = MutableStateFlow(emptyList<Budget>())
        override suspend fun getBudgets() = emptyList<Budget>()
        override suspend fun applyBudgets(save: List<Budget>, deleteIds: List<String>) = Unit
    }
    private val source = BudgetSource(
        noBudgets, categories, transactions, FakeReportRepository(), FakeExchangeRateRepository(),
        FakeReportCurrencySettings(), monthStart, tz,
    )

    private fun operation(id: String, category: String, day: LocalDate, type: TransactionType = TransactionType.EXPENSE) =
        Transaction(id, type, "acc", Money(100, ars), day.atStartOfDayIn(tz), categoryId = category)

    @Test
    fun `the budget list reads no operations`() = runTest {
        source.observe().first()

        assertEquals(emptyList<TransactionFilter>(), transactions.queries)
    }

    @Test
    fun `an envelope asks for its period and keeps the folder's spending, newest first`() = runTest {
        categories.saveCategories(
            listOf(
                Category("home", "Дом", CategoryType.EXPENSE),
                Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
                Category("cafe", "Кафе", CategoryType.EXPENSE),
            ),
        )
        listOf(
            operation("a", "home", LocalDate(2026, 10, 25)),
            operation("b", "rent", LocalDate(2026, 11, 24)),
            operation("c", "rent", LocalDate(2026, 11, 25)),
            operation("d", "cafe", LocalDate(2026, 11, 1)),
            operation("e", "rent", LocalDate(2026, 11, 2), TransactionType.INCOME),
        ).forEach { transactions.saveTransaction(it) }

        val envelope = source.observeEnvelope("home", YearMonth(2026, Month.OCTOBER)).first()

        assertEquals(listOf("b", "e", "a"), envelope.operations.map { it.id })
        assertEquals(
            listOf(TransactionFilter(from = LocalDate(2026, 10, 25).atStartOfDayIn(tz), to = LocalDate(2026, 11, 25).atStartOfDayIn(tz))),
            transactions.queries,
        )
    }
}

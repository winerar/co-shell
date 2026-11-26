package app.coshell.budgets.list

import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.budgets.BudgetRepository
import app.coshell.core.domain.budgets.SaveBudgetUseCase
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import app.coshell.core.model.MoneyFormat
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeReportCurrencySettings
import app.coshell.testing.FakeReportRepository
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atStartOfDayIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BudgetsViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val monthStart = object : MonthStartSettings {
        override val monthStartDay = MutableStateFlow(MonthStartSettings.DEFAULT)
        override suspend fun setMonthStartDay(day: Int) {
            require(day in MonthStartSettings.DAYS)
            monthStartDay.value = day
        }
    }
    private val limits = MutableStateFlow(emptyList<Budget>())
    private val noBudgets = object : BudgetRepository {
        override fun observeBudgets() = limits
        override suspend fun getBudgets() = limits.value
        override suspend fun applyBudgets(save: List<Budget>, deleteIds: List<String>) {
            limits.value = limits.value.filter { it.id !in deleteIds && it.id !in save.map(Budget::id) } + save
        }
    }
    private val categories = FakeCategoryRepository().apply {
        items.value = listOf(
            Category("food", "Продукты", CategoryType.EXPENSE),
            Category("cafe", "Кафе", CategoryType.EXPENSE),
        ).associateBy { it.id }
    }
    private val reports = FakeReportRepository()

    /** «Today» is 12 November 2026. */
    private val clock = object : Clock {
        override fun now(): Instant = LocalDate(2026, 11, 12).atStartOfDayIn(TimeZone.UTC)
    }

    private fun viewModel() = BudgetsViewModel(
        BudgetSource(
            noBudgets,
            categories,
            FakeTransactionRepository(),
            reports,
            FakeExchangeRateRepository(),
            FakeReportCurrencySettings(),
            monthStart,
            TimeZone.UTC,
        ),
        monthStart,
        SaveBudgetUseCase(categories, noBudgets),
        MoneyFormat.RU,
        clock,
        TimeZone.UTC,
    ).apply { work = EmptyCoroutineContext }

    @Test
    fun `a new start day is saved and moves the current month and its days`() = runTest {
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)
        val calendar = vm.state.first { it.month != null }
        assertEquals(1, calendar.startDay)
        assertEquals(YearMonth(2026, Month.NOVEMBER), calendar.month)
        assertNull(calendar.periodDays)

        vm.onStartDayChange(25)

        // On the 12th a month from the 25th is still October's: 25 Oct — 24 Nov.
        val payday = vm.state.first { it.startDay == 25 }
        assertEquals(25, monthStart.monthStartDay.value)
        assertEquals(YearMonth(2026, Month.OCTOBER), payday.month)
        assertEquals(LocalDate(2026, 10, 25) to LocalDate(2026, 11, 24), payday.periodDays)
    }

    private fun spend(category: String, major: Long, month: Int) {
        reports.days.value += ReportDay(LocalDate(2026, month, 10), category, TransactionType.EXPENSE, Money.ofMajor(major, currency = CurrencyCode("USD")))
    }

    @Test
    fun `without limits the earlier spending is offered as limits, ticked`() = runTest {
        listOf(8, 9, 10).forEach { spend("food", 400, it) }
        listOf(8, 9, 10).forEach { spend("cafe", 78, it) }
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)

        val offer = vm.state.first { it.month != null }.offer!!

        assertEquals(listOf("food", "cafe"), offer.rows.map { it.categoryId })
        assertEquals(listOf("400${MoneyFormat.NBSP}USD", "78${MoneyFormat.NBSP}USD"), offer.rows.map { it.amount })
        assertEquals(2, offer.checkedCount)
    }

    @Test
    fun `nothing is offered without history, and not once a limit exists`() = runTest {
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)
        assertNull(vm.state.first { it.month != null }.offer)

        spend("food", 400, 10)
        assertTrue(vm.state.first { it.offer != null }.offer!!.rows.isNotEmpty())
        vm.onCreateSuggested()

        assertNull(vm.state.first { it.envelopes.isNotEmpty() }.offer)
    }

    @Test
    fun `create makes limits of the ticked rows only, from the shown month`() = runTest {
        spend("food", 400, 10)
        spend("cafe", 78, 10)
        val vm = viewModel()
        vm.state.launchIn(backgroundScope)
        vm.state.first { it.offer != null }

        vm.onToggleSuggestion("cafe")
        assertEquals(1, vm.state.first { it.offer?.checkedCount == 1 }.offer!!.checkedCount)
        vm.onCreateSuggested()

        vm.state.first { it.envelopes.isNotEmpty() }
        assertEquals(listOf("food"), limits.value.map { it.categoryId })
        assertEquals(YearMonth(2026, Month.NOVEMBER), limits.value.single().startMonth)
        assertEquals(Money.ofMajor(400, currency = CurrencyCode("USD")), limits.value.single().limit)
    }
}

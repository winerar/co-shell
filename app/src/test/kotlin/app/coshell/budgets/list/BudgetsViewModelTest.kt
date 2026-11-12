package app.coshell.budgets.list

import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.budgets.BudgetRepository
import app.coshell.core.domain.settings.MonthStartSettings
import app.coshell.core.model.Budget
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
    private val noBudgets = object : BudgetRepository {
        override fun observeBudgets() = MutableStateFlow(emptyList<Budget>())
        override suspend fun getBudgets() = emptyList<Budget>()
        override suspend fun applyBudgets(save: List<Budget>, deleteIds: List<String>) = Unit
    }

    /** «Today» is 12 November 2026. */
    private val clock = object : Clock {
        override fun now(): Instant = LocalDate(2026, 11, 12).atStartOfDayIn(TimeZone.UTC)
    }

    private fun viewModel() = BudgetsViewModel(
        BudgetSource(
            noBudgets,
            FakeCategoryRepository(),
            FakeTransactionRepository(),
            FakeReportRepository(),
            FakeExchangeRateRepository(),
            FakeReportCurrencySettings(),
            monthStart,
            TimeZone.UTC,
        ),
        monthStart,
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
}

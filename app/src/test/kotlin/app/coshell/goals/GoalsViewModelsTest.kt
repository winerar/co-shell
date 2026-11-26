package app.coshell.goals

import androidx.lifecycle.SavedStateHandle
import app.coshell.core.domain.goals.InMemoryGoalRepository
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Goal
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.goals.edit.GoalEditViewModel
import app.coshell.goals.edit.GoalError
import app.coshell.goals.list.GoalsViewModel
import app.coshell.navigation.GoalEditDestination
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeReportCurrencySettings
import app.coshell.testing.MainDispatcherRule
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** «Цели» (#768): the list with totals and the goal sheet. */
class GoalsViewModelsTest {
    @get:Rule val main = MainDispatcherRule()

    private val usd = CurrencyCode("USD")
    private val now = Instant.parse("2026-11-26T12:00:00Z")
    private val clock = object : Clock { override fun now() = now }
    private val accounts = FakeAccountRepository()
    private val goals = InMemoryGoalRepository()
    private val nbsp = MoneyFormat.NBSP

    private suspend fun seed() = accounts.saveAccount(Account("box", "Копилка", usd, AccountType.OTHER, initialBalanceMinor = 1_250_00))

    private fun source() = GoalsSource(goals, accounts, FakeExchangeRateRepository(), FakeReportCurrencySettings(listOf(usd)), clock, TimeZone.UTC)

    @Test
    fun `lists goals with what is saved, the plan a month and the totals`() = runTest {
        seed()
        goals.save(Goal("trip", "Отпуск", Money(2_000_00, usd), "box", LocalDate(2027, 6, 26)))
        goals.save(Goal("pillow", "Подушка", Money(100_00, usd), "box"))
        val vm = GoalsViewModel(source(), MoneyFormat.RU)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

        val s = vm.state.value
        // The trip is due first and takes 1 250; the pillow without a deadline gets nothing.
        assertEquals(listOf("trip", "pillow"), s.active.map { it.id })
        assertEquals("1${MoneyFormat.NARROW_NBSP}250${nbsp}USD", s.active[0].saved)
        assertEquals(GoalWhen.DEADLINE, s.active[0].whenKind)
        assertEquals("107,15${nbsp}USD", s.active[0].perMonth)
        assertEquals(GoalWhen.NO_DEADLINE, s.active[1].whenKind)
        assertEquals("1${MoneyFormat.NARROW_NBSP}250${nbsp}USD", s.totals.saved)
    }

    @Test
    fun `the sheet checks the form and saves a goal on the account`() = runTest {
        seed()
        val vm = GoalEditViewModel(SavedStateHandle(), goals, accounts, IdGenerator { "g1" }, MoneyFormat.RU, clock, TimeZone.UTC)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

        vm.save()
        assertEquals(setOf(GoalError.NAME, GoalError.TARGET), vm.state.value.errors)

        vm.onName("Отпуск")
        vm.onAmount("2000")
        vm.onYearly(true)
        vm.save()
        assertEquals(setOf(GoalError.YEARLY), vm.state.value.errors)

        vm.onDeadline(1_813_968_000_000L) // 26 June 2027, UTC
        assertTrue(vm.state.value.planPerMonth.isNotEmpty())
        vm.save()
        assertTrue(vm.state.value.isDone)
        val saved = goals.observe().first().single()
        assertEquals(Goal("g1", "Отпуск", Money(2_000_00, usd), "box", LocalDate(2027, 6, 26), yearly = true), saved)
    }

    @Test
    fun `an existing goal shows its progress and can be deleted`() = runTest {
        seed()
        goals.save(Goal("trip", "Отпуск", Money(2_000_00, usd), "box"))
        val vm = GoalEditViewModel(SavedStateHandle(mapOf(GoalEditDestination.ARG_GOAL_ID to "trip")), goals, accounts, IdGenerator { "x" }, MoneyFormat.RU, clock, TimeZone.UTC)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

        val p = vm.state.value.progress
        assertEquals(0.625f, p?.fraction ?: 0f, 0.001f)
        assertEquals(75_000L, vm.state.value.topUpMinor)
        vm.delete()
        assertTrue(goals.observe().first().isEmpty())
    }
}

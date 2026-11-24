package app.coshell.debts

import app.coshell.core.domain.debts.CloseDebtUseCase
import app.coshell.core.domain.debts.InMemoryDebtRepository
import app.coshell.core.domain.debts.IssueDebtUseCase
import app.coshell.core.domain.debts.ReturnDebtUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.DebtDirection
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.debts.edit.DebtNewViewModel
import app.coshell.debts.list.DebtDue
import app.coshell.debts.list.DebtsViewModel
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.MainDispatcherRule
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** «Долги» (#552): the list, its tabs and the new-debt sheet. */
class DebtsViewModelsTest {
    @get:Rule val main = MainDispatcherRule()

    private val usd = CurrencyCode("USD")
    private val now = Instant.parse("2026-11-23T12:00:00Z")
    private val clock = object : Clock { override fun now() = now }
    private val accounts = FakeAccountRepository()
    private val debts = InMemoryDebtRepository()
    private var counter = 0
    private val ids = IdGenerator { "id${counter++}" }
    private val issue = IssueDebtUseCase(debts, accounts, ids)

    private suspend fun seed() = accounts.saveAccount(Account("cash", "Наличные", usd, AccountType.CASH))

    private fun list() = DebtsViewModel(debts, MoneyFormat.RU, clock, TimeZone.UTC)

    @Test
    fun `lists debts by tab with a total per currency and the due stage`() = runTest {
        seed()
        issue(DebtDirection.LENT, "Саша", Money(30_000, usd), "cash", now, LocalDate(2026, 11, 25))
        issue(DebtDirection.LENT, "Лена", Money(5_000, usd), "cash", now, LocalDate(2026, 11, 19))
        issue(DebtDirection.BORROWED, "Банк", Money(10_000, usd), "cash", now)
        val vm = list()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

        val lent = vm.state.value
        assertEquals(listOf("350,00" + MoneyFormat.NBSP + "USD"), lent.totals)
        assertEquals(listOf("Саша", "Лена"), lent.active.map { it.person })
        assertEquals(listOf(DebtDue.SOON, DebtDue.LATE), lent.active.map { it.due })
        assertFalse(lent.nothingAtAll)

        vm.selectTab(1)
        assertEquals(listOf("Банк"), vm.state.value.active.map { it.person })
    }

    @Test
    fun `a returned debt keeps its fraction and a closed one folds away`() = runTest {
        seed()
        val saved = issue(DebtDirection.LENT, "Саша", Money(30_000, usd), "cash", now) as app.coshell.core.domain.debts.DebtResult.Saved
        ReturnDebtUseCase(debts, accounts, ids)(saved.debtId, Money(10_000, usd), "cash", now)
        val other = issue(DebtDirection.LENT, "Мама", Money(1_000, usd), "cash", now) as app.coshell.core.domain.debts.DebtResult.Saved
        CloseDebtUseCase(debts, ids)(other.debtId, now)
        val vm = list()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

        val row = vm.state.value.active.single()
        assertEquals("200,00" + MoneyFormat.NBSP + "USD", row.left)
        assertEquals(1f / 3, row.returnedFraction, 0.001f)
        assertEquals(listOf("Мама"), vm.state.value.closed.map { it.person })
    }

    @Test
    fun `the empty book shows nothing at all`() = runTest {
        val vm = list()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        assertTrue(vm.state.value.nothingAtAll)
    }

    @Test
    fun `the new sheet checks the form and writes one issue event`() = runTest {
        seed()
        val vm = DebtNewViewModel(accounts, issue, clock, TimeZone.UTC)
        vm.save()
        assertTrue(vm.state.value.personError)
        assertTrue(vm.state.value.amountError)

        vm.onPerson("Саша"); vm.onAmount("300")
        vm.save()
        assertTrue(vm.state.value.isDone)
        assertEquals(listOf("Саша"), debts.events().map { it.person })
        assertEquals(30_000L, debts.events().single().amount.amountMinor)
    }
}

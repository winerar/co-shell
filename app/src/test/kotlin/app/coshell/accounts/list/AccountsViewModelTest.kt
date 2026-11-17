package app.coshell.accounts.list

import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.testing.FakeAccountRepository
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import app.coshell.core.domain.settings.InMemoryScreenOptions
import app.coshell.core.domain.settings.ScreenOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runTest

class AccountsViewModelTest {
    private val ars = CurrencyCode("ARS")

    @Test
    fun `archived accounts are split out`() {
        val vm = AccountsViewModel(FakeAccountRepository(), MoneyFormat.RU)
        val state = vm.buildState(
            listOf(
                AccountWithBalance(Account("a", "Карта", ars), Money(-5_00, ars)),
                AccountWithBalance(Account("b", "Старая", ars, archived = true), Money(0, ars)),
            ),
            showArchived = false,
        )
        assertEquals(listOf("a"), state.active.map { it.id })
        assertEquals(listOf("b"), state.archived.map { it.id })
        assertEquals(true, state.active.single().isNegative)
    }

    @Test
    fun `personal accounts are labelled only when the family has other members`() {
        val vm = AccountsViewModel(FakeAccountRepository(), MoneyFormat.RU)
        val items = listOf(
            AccountWithBalance(Account("shared", "Общий", ars), Money(0, ars)),
            AccountWithBalance(Account("mine", "Мой", ars, isShared = false, ownerMemberId = "me"), Money(0, ars)),
            AccountWithBalance(Account("hers", "Её", ars, isShared = false, ownerMemberId = "wife"), Money(0, ars)),
        )
        val labels = vm.buildState(items, showArchived = false, others = mapOf("wife" to "Маша")).active.associate { it.id to it.personalOf }
        assertEquals(mapOf("shared" to null, "mine" to "", "hers" to "Маша"), labels)
        assertEquals(listOf(null, null, null), vm.buildState(items, showArchived = false).active.map { it.personalOf })
    }

    @Test
    fun `converted balance keeps the approx sign glued to the amount`() {
        val usd = CurrencyCode("USD")
        val vm = AccountsViewModel(FakeAccountRepository(), MoneyFormat.RU)
        val rates = listOf(ExchangeRate(LocalDate(2026, 11, 10), usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED))
        val state = vm.buildState(
            listOf(AccountWithBalance(Account("a", "Карта", ars), Money(1_000_000_00, ars))),
            showArchived = false,
            conversion = Conversion(rates, usd, LocalDate(2026, 11, 10)),
        )
        // A no-break space: when there is no room the amount shrinks, «≈» is not left alone on the line (#431).
        assertEquals("≈\u00A01\u202F000,00\u00A0USD", state.active.single().converted)
        assertEquals(state.active.single().converted, state.total)
    }

    @Test
    fun `opens grouped by currency with the archive when the settings say so`() = runTest {
        val repo = FakeAccountRepository().apply {
            items.value = mapOf(
                "a" to Account("a", "Карта", ars),
                "b" to Account("b", "Старая", ars, archived = true),
            )
        }
        val vm = AccountsViewModel(repo, MoneyFormat.RU, screenOptions = InMemoryScreenOptions(ScreenOptions(accountsByCurrency = true, accountsShowArchived = true)))
        vm.state.launchIn(backgroundScope)
        val state = vm.state.first { it.grouping == AccountGrouping.CURRENCY }
        assertEquals(true, state.showArchived)
    }
}

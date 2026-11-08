package app.coshell.accounts.list

import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.testing.FakeAccountRepository
import org.junit.Assert.assertEquals
import org.junit.Test

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
}

package app.coshell.home

import app.coshell.core.domain.AccountRepository
import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {
    private val rub = CurrencyCode("RUB")
    private val usd = CurrencyCode("USD")

    private val noAccounts = object : AccountRepository {
        override fun observeAccounts(includeArchived: Boolean): Flow<List<Account>> = emptyFlow()
        override fun observeAccountsWithBalance(includeArchived: Boolean): Flow<List<AccountWithBalance>> = emptyFlow()
        override suspend fun getAccount(id: String): Account? = null
        override suspend fun saveAccount(account: Account) = Unit
        override suspend fun deleteAccount(id: String) = Unit
    }

    @Test
    fun `totals are summed per currency`() {
        val viewModel = HomeViewModel(noAccounts, MoneyFormat.RU)
        val state = viewModel.buildState(
            listOf(
                AccountWithBalance(Account("a", "Карта", rub), Money(100_00, rub)),
                AccountWithBalance(Account("b", "Кредитка", rub), Money(-30_00, rub)),
                AccountWithBalance(Account("c", "Bybit", usd), Money(5_00, usd)),
            ),
        )
        assertEquals(2, state.totals.size)
        assertEquals(MoneyFormat.RU.format(Money(70_00, rub)), state.totals[0])
        assertEquals(listOf(false, true, false), state.accounts.map { it.isNegative })
    }
}

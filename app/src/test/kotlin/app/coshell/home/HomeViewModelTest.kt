package app.coshell.home

import app.coshell.core.domain.AccountRepository
import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeReportCurrencySettings
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeViewModelTest {
    private val rub = CurrencyCode("RUB")
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val nov5 = LocalDate(2026, 11, 5)
    private val nov9 = LocalDate(2026, 11, 9)

    private val noAccounts = object : AccountRepository {
        override fun observeAccounts(includeArchived: Boolean): Flow<List<Account>> = emptyFlow()
        override fun observeAccountsWithBalance(includeArchived: Boolean): Flow<List<AccountWithBalance>> = emptyFlow()
        override suspend fun getAccount(id: String): Account? = null
        override suspend fun saveAccount(account: Account) = Unit
        override suspend fun deleteAccount(id: String) = Unit
    }
    private val viewModel = HomeViewModel(
        noAccounts,
        FakeExchangeRateRepository(),
        FakeReportCurrencySettings(),
        MoneyFormat.RU,
        Clock.System,
        TimeZone.UTC,
    )

    private val accounts = listOf(
        AccountWithBalance(Account("a", "Карта", ars), Money(150_000_00, ars)),
        AccountWithBalance(Account("b", "Кредитка", ars), Money(-50_000_00, ars)),
        AccountWithBalance(Account("c", "Bybit", usd), Money(5_00, usd)),
    )
    private val rates = listOf(ExchangeRate(nov5, usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED))

    @Test
    fun `totals are shown in every report currency, main first`() {
        val state = viewModel.buildState(accounts, rates, listOf(usd, ars), nov9)

        assertEquals(listOf(MoneyFormat.RU.format(Money(105_00, usd)), MoneyFormat.RU.format(Money(105_000_00, ars))), state.totals)
        assertEquals(nov5, state.rateDate)
        assertEquals(emptyList<String>(), state.missing)
        assertEquals(listOf(MoneyFormat.RU.format(Money(100_000_00, ars)), MoneyFormat.RU.format(Money(5_00, usd))), state.byCurrency)
        assertEquals(listOf(false, true, false), state.accounts.map { it.isNegative })
    }

    @Test
    fun `currencies without rates are left out and named`() {
        val withRub = accounts + AccountWithBalance(Account("d", "Сбер", rub), Money(1_000_00, rub))

        val state = viewModel.buildState(withRub, rates, listOf(usd), nov9)

        assertEquals(listOf(MoneyFormat.RU.format(Money(105_00, usd))), state.totals)
        assertEquals(listOf("RUB"), state.missing)
    }

    @Test
    fun `one currency needs no rate date and no breakdown`() {
        val state = viewModel.buildState(accounts.take(2), emptyList(), listOf(ars), nov9)

        assertEquals(listOf(MoneyFormat.RU.format(Money(100_000_00, ars))), state.totals)
        assertNull(state.rateDate)
        assertEquals(emptyList<String>(), state.byCurrency)
    }
}

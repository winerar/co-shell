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
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeReportCurrencySettings
import app.coshell.testing.FakeTransactionRepository
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
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
        FakeTransactionRepository(),
        FakeExchangeRateRepository(),
        FakeReportCurrencySettings(),
        FakeCategoryRepository(),
        { flowOf(emptyMap()) },
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
        val state = viewModel.buildState(accounts, emptyList(), rates, listOf(usd, ars), nov9)

        assertEquals(listOf(MoneyFormat.RU.format(Money(105_00, usd)), MoneyFormat.RU.format(Money(105_000_00, ars))), state.totals)
        assertEquals(nov5, state.rateDate)
        assertEquals(emptyList<String>(), state.missing)
        assertEquals(listOf(MoneyFormat.RU.format(Money(100_000_00, ars)), MoneyFormat.RU.format(Money(5_00, usd))), state.byCurrency)
        assertEquals(listOf(false, true, false), state.accounts.map { it.isNegative })
    }

    @Test
    fun `currencies without rates are left out and named`() {
        val withRub = accounts + AccountWithBalance(Account("d", "Сбер", rub), Money(1_000_00, rub))

        val state = viewModel.buildState(withRub, emptyList(), rates, listOf(usd), nov9)

        assertEquals(listOf(MoneyFormat.RU.format(Money(105_00, usd))), state.totals)
        assertEquals(listOf("RUB"), state.missing)
    }

    @Test
    fun `excluded account is left out of totals and breakdown but stays in the list`() {
        val debt = AccountWithBalance(Account("d", "Долг", rub, excludedFromTotal = true), Money(-1_000_00, rub))

        val state = viewModel.buildState(accounts + debt, emptyList(), rates, listOf(usd), nov9)

        assertEquals(listOf(MoneyFormat.RU.format(Money(105_00, usd))), state.totals)
        assertEquals(emptyList<String>(), state.missing)
        assertEquals(listOf(MoneyFormat.RU.format(Money(100_000_00, ars)), MoneyFormat.RU.format(Money(5_00, usd))), state.byCurrency)
        assertEquals(listOf(false, false, false, true), state.accounts.map { it.excludedFromTotal })
    }

    @Test
    fun `one currency needs no rate date and no breakdown`() {
        val state = viewModel.buildState(accounts.take(2), emptyList(), emptyList(), listOf(ars), nov9)

        assertEquals(listOf(MoneyFormat.RU.format(Money(100_000_00, ars))), state.totals)
        assertNull(state.rateDate)
        assertEquals(emptyList<String>(), state.byCurrency)
    }

    @Test
    fun `month spending is shown in every report currency`() {
        val expenses = listOf(
            Transaction("t1", TransactionType.EXPENSE, "a", Money(20_000_00, ars), nov9.atStartOfDayIn(TimeZone.UTC)),
            Transaction("t2", TransactionType.EXPENSE, "c", Money(3_00, usd), nov9.atStartOfDayIn(TimeZone.UTC)),
            Transaction("t3", TransactionType.INCOME, "c", Money(100_00, usd), nov9.atStartOfDayIn(TimeZone.UTC)),
        )

        val spending = viewModel.buildState(accounts, expenses, rates, listOf(usd, ars), nov9).spending!!

        assertEquals(LocalDate(2026, 11, 1), spending.month)
        assertEquals(listOf(MoneyFormat.RU.format(Money(23_00, usd)), MoneyFormat.RU.format(Money(23_000_00, ars))), spending.totals)
        assertEquals(true, spending.converted)
    }

    @Test
    fun `month without expenses shows zero and no rate hint`() {
        val spending = viewModel.buildState(accounts, emptyList(), rates, listOf(usd), nov9).spending!!

        assertEquals(listOf(MoneyFormat.RU.format(Money(0, usd))), spending.totals)
        assertEquals(false, spending.converted)
    }

    @Test
    fun `month income is summed in every report currency`() {
        val items = listOf(
            Transaction("t1", TransactionType.INCOME, "a", Money(10_000_00, ars), nov9.atStartOfDayIn(TimeZone.UTC)),
            Transaction("t2", TransactionType.EXPENSE, "c", Money(3_00, usd), nov9.atStartOfDayIn(TimeZone.UTC)),
        )

        val income = viewModel.buildState(accounts, items, rates, listOf(usd, ars), nov9).income!!

        assertEquals(listOf(MoneyFormat.RU.format(Money(10_00, usd)), MoneyFormat.RU.format(Money(10_000_00, ars))), income.totals)
    }

    @Test
    fun `a refund into an expense category lowers spending and is not income`() {
        val items = listOf(
            Transaction("t1", TransactionType.EXPENSE, "a", Money(10_000_00, ars), nov9.atStartOfDayIn(TimeZone.UTC), categoryId = "cafe"),
            Transaction("t2", TransactionType.INCOME, "a", Money(4_000_00, ars), nov9.atStartOfDayIn(TimeZone.UTC), categoryId = "cafe"),
        )

        val state = viewModel.buildState(accounts, items, rates, listOf(ars), nov9, expenseCategoryIds = setOf("cafe"))

        assertEquals(listOf(MoneyFormat.RU.format(Money(6_000_00, ars))), state.spending!!.totals)
        assertEquals(listOf(MoneyFormat.RU.format(Money(0, ars))), state.income!!.totals)
    }

    @Test
    fun `daily bars are in the main currency with today marked`() {
        val items = listOf(
            Transaction("t1", TransactionType.EXPENSE, "a", Money(2_000_00, ars), LocalDate(2026, 11, 2).atStartOfDayIn(TimeZone.UTC)),
            Transaction("t2", TransactionType.EXPENSE, "c", Money(3_00, usd), nov9.atStartOfDayIn(TimeZone.UTC)),
        )

        val spending = viewModel.buildState(accounts, items, rates, listOf(usd, ars), nov9).spending!!

        assertEquals(30, spending.daily.size)
        assertEquals(2_00L, spending.daily[1])
        assertEquals(3_00L, spending.daily[8])
        assertEquals(8, spending.today)
    }

    @Test
    fun `attention lists missing rates, old rates and operations without a category`() {
        val withRub = accounts + AccountWithBalance(Account("r", "Рубли", rub), Money(1_000_00, rub))
        val items = listOf(
            Transaction("t1", TransactionType.EXPENSE, "a", Money(1_00, ars), nov9.atStartOfDayIn(TimeZone.UTC)),
            Transaction("t2", TransactionType.EXPENSE, "a", Money(1_00, ars), nov9.atStartOfDayIn(TimeZone.UTC), categoryId = "food"),
            Transaction("t3", TransactionType.TRANSFER, "a", Money(1_00, ars), nov9.atStartOfDayIn(TimeZone.UTC), toAccountId = "b", toAmount = Money(1_00, ars)),
        )

        val attention = viewModel.buildState(withRub, items, rates, listOf(usd), nov9).attention

        assertEquals(listOf("RUB"), attention.missingRates)
        assertEquals(nov5, attention.staleRatesSince) // 4 days old: the daily download keeps failing
        assertEquals(1, attention.uncategorized)
        assertEquals(3, attention.count)
        assertEquals(true, attention.isRatesIssue)
    }

    @Test
    fun `fresh rates and categorised operations need no attention`() {
        val attention = viewModel.buildState(accounts, emptyList(), rates, listOf(usd), LocalDate(2026, 11, 7)).attention

        assertNull(attention.staleRatesSince)
        assertEquals(0, attention.count)
    }
}

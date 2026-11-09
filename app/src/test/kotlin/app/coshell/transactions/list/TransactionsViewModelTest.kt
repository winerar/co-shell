package app.coshell.transactions.list

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TransactionsViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test
    fun `list follows the repository`() = runTest {
        val ars = CurrencyCode("ARS")
        val now = Instant.fromEpochSeconds(1_794_000_000)
        val accounts = FakeAccountRepository().apply { saveAccount(Account("card", "Карта", ars)) }
        val transactions = FakeTransactionRepository()
        val vm = TransactionsViewModel(
            transactions, accounts, FakeCategoryRepository(), { flowOf(emptyMap()) }, MoneyFormat.RU,
            object : Clock { override fun now() = now }, TimeZone.UTC,
        )
        vm.state.launchIn(backgroundScope)
        assertEquals(emptyList<TransactionsUiState.Day>(), vm.state.first { !it.isLoading }.days)

        transactions.saveTransaction(Transaction("t", TransactionType.EXPENSE, "card", Money(100, ars), now))
        val days = vm.state.value.days
        assertEquals(listOf("t"), days.single().rows.map { it.id })
        assertEquals(TransactionsUiState.RelativeDay.TODAY, days.single().relative)
    }
}

package app.coshell.transactions.list

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.transactions.DeleteTransactionWithUndo
import app.coshell.undo.UndoCenter
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeReportCurrencySettings
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
            DeleteTransactionWithUndo(transactions, UndoCenter()), FakeExchangeRateRepository(), FakeReportCurrencySettings(),
        )
        vm.state.launchIn(backgroundScope)
        assertEquals(emptyList<TransactionsUiState.Day>(), vm.state.first { !it.isLoading }.days)

        transactions.saveTransaction(Transaction("t", TransactionType.EXPENSE, "card", Money(100, ars), now))
        val days = vm.state.value.days
        assertEquals(listOf("t"), days.single().rows.map { it.id })
        assertEquals(TransactionsUiState.RelativeDay.TODAY, days.single().relative)
        assertEquals(false, vm.state.value.hasMore)
    }

    @Test
    fun `loads older transactions a page at a time`() = runTest {
        val ars = CurrencyCode("ARS")
        val now = Instant.fromEpochSeconds(1_794_000_000)
        val transactions = FakeTransactionRepository()
        val total = GrowingWindow.PAGE_SIZE + 5
        repeat(total) { i ->
            transactions.saveTransaction(
                Transaction("t$i", TransactionType.EXPENSE, "card", Money(100, ars), Instant.fromEpochSeconds(now.epochSeconds - i * 3_600L)),
            )
        }
        val vm = TransactionsViewModel(
            transactions, FakeAccountRepository(), FakeCategoryRepository(), { flowOf(emptyMap()) }, MoneyFormat.RU,
            object : Clock { override fun now() = now }, TimeZone.UTC,
            DeleteTransactionWithUndo(transactions, UndoCenter()), FakeExchangeRateRepository(), FakeReportCurrencySettings(),
        )
        vm.state.launchIn(backgroundScope)
        val first = vm.state.first { !it.isLoading }
        assertEquals(GrowingWindow.PAGE_SIZE, first.days.sumOf { it.rows.size })
        assertEquals(true, first.hasMore)

        vm.loadMore()
        val all = vm.state.first { it.days.sumOf { d -> d.rows.size } == total }
        assertEquals(false, all.hasMore)
    }

    @Test
    fun `typing a search filters in memory without new database queries`() = runTest {
        val ars = CurrencyCode("ARS")
        val now = Instant.fromEpochSeconds(1_794_000_000)
        val transactions = FakeTransactionRepository()
        transactions.saveTransaction(Transaction("taxi", TransactionType.EXPENSE, "card", Money(100, ars), now, note = "Такси"))
        transactions.saveTransaction(Transaction("tea", TransactionType.EXPENSE, "card", Money(200, ars), now, note = "Чай"))
        val vm = TransactionsViewModel(
            transactions, FakeAccountRepository(), FakeCategoryRepository(), { flowOf(emptyMap()) }, MoneyFormat.RU,
            object : Clock { override fun now() = now }, TimeZone.UTC,
            DeleteTransactionWithUndo(transactions, UndoCenter()), FakeExchangeRateRepository(), FakeReportCurrencySettings(),
        )
        vm.state.launchIn(backgroundScope)
        vm.state.first { !it.isLoading }

        listOf("т", "та", "так").forEach { q -> vm.onFilter { it.copy(query = q) } }
        assertEquals(listOf("taxi"), vm.state.value.days.flatMap { d -> d.rows.map { it.id } })
        // The page query, then one unlimited query for the whole search — not one per letter.
        assertEquals(listOf(GrowingWindow.PAGE_SIZE, null), transactions.queries.map { it.limit })

        vm.onFilter { it.copy(kind = TransactionsUiState.Kind.INCOME) }
        assertEquals(TransactionType.INCOME, transactions.queries.last().type)
        assertEquals(emptyList<String>(), vm.state.value.days.flatMap { d -> d.rows.map { it.id } })
    }

    @Test
    fun `a set filter loads everything for its total and still shows a page at a time`() = runTest {
        val ars = CurrencyCode("ARS")
        val now = Instant.fromEpochSeconds(1_794_000_000)
        val transactions = FakeTransactionRepository()
        val total = GrowingWindow.PAGE_SIZE + 5
        repeat(total) { i ->
            transactions.saveTransaction(
                Transaction("t$i", TransactionType.EXPENSE, "card", Money(100_00, ars), Instant.fromEpochSeconds(now.epochSeconds - i * 3_600L)),
            )
        }
        val vm = TransactionsViewModel(
            transactions, FakeAccountRepository(), FakeCategoryRepository(), { flowOf(emptyMap()) }, MoneyFormat.RU,
            object : Clock { override fun now() = now }, TimeZone.UTC,
            DeleteTransactionWithUndo(transactions, UndoCenter()), FakeExchangeRateRepository(), FakeReportCurrencySettings(),
        )
        vm.state.launchIn(backgroundScope)
        vm.state.first { !it.isLoading }
        assertEquals(null, vm.state.value.total)

        vm.onFilter { it.copy(kind = TransactionsUiState.Kind.EXPENSE) }
        val filtered = vm.state.first { it.total != null }
        assertEquals(null, transactions.queries.last().limit)
        assertEquals(total, filtered.total?.count)
        assertEquals(MoneyFormat.RU.format(Money(-total * 100_00L, ars)), filtered.total?.amount)
        assertEquals(GrowingWindow.PAGE_SIZE, filtered.days.sumOf { it.rows.size })
        assertEquals(true, filtered.hasMore)

        vm.loadMore()
        val all = vm.state.first { it.days.sumOf { d -> d.rows.size } == total }
        assertEquals(false, all.hasMore)
    }

    @Test
    fun `selection deletes the chosen rows and ends`() = runTest {
        val ars = CurrencyCode("ARS")
        val now = Instant.fromEpochSeconds(1_794_000_000)
        val transactions = FakeTransactionRepository()
        listOf("a", "b", "c").forEach { transactions.saveTransaction(Transaction(it, TransactionType.EXPENSE, "card", Money(100, ars), now)) }
        val vm = TransactionsViewModel(
            transactions, FakeAccountRepository(), FakeCategoryRepository(), { flowOf(emptyMap()) }, MoneyFormat.RU,
            object : Clock { override fun now() = now }, TimeZone.UTC,
            DeleteTransactionWithUndo(transactions, UndoCenter()), FakeExchangeRateRepository(), FakeReportCurrencySettings(),
        )
        vm.state.launchIn(backgroundScope)
        vm.state.first { !it.isLoading }

        vm.onLongPress("a")
        vm.onToggle("b")
        assertEquals(setOf("a", "b"), vm.selection.value.ids)

        vm.deleteSelected()
        assertEquals(false, vm.selection.value.active)
        val left = vm.state.first { it.days.sumOf { d -> d.rows.size } == 1 }
        assertEquals(listOf("c"), left.days.single().rows.map { it.id })
    }
}

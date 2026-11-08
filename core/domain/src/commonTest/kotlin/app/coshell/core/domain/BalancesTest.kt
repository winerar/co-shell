package app.coshell.core.domain

import app.coshell.core.domain.usecase.ObserveAccountsWithBalances
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BalancesTest {
    private val history = listOf(
        tx("1", TransactionType.EXPENSE, Money(30_000, RUB), category = food),
        tx("2", TransactionType.INCOME, Money(50_000, RUB), category = salary),
        // Currency exchange: 10 000.00 ₽ → 100.00 €
        tx("3", TransactionType.TRANSFER, Money(1_000_000, RUB), to = eurCash, toAmount = Money(10_000, EUR)),
        tx("4", TransactionType.EXPENSE, Money(2_500, EUR), account = eurCash, category = food),
    )

    @Test
    fun `balance starts from the initial balance`() {
        assertEquals(Money(100_000, RUB), balanceOf(rubCard, emptyList()))
    }

    @Test
    fun `balance applies expenses, incomes and both sides of a transfer`() {
        assertEquals(Money(100_000 - 30_000 + 50_000 - 1_000_000, RUB), balanceOf(rubCard, history))
        assertEquals(Money(10_000 - 2_500, EUR), balanceOf(eurCash, history))
    }

    @Test
    fun `deleted transactions do not count`() {
        val deleted = history[0].copy(meta = testMeta.copy(deleted = true))
        assertEquals(Money(100_000, RUB), balanceOf(rubCard, listOf(deleted)))
    }

    @Test
    fun `observes balances and reacts to new transactions`() = runTest {
        val accounts = FakeAccountRepository().apply { upsert(rubCard); upsert(eurCash) }
        val transactions = FakeTransactionRepository()
        val observe = ObserveAccountsWithBalances(accounts, transactions)

        assertEquals(listOf(Money(100_000, RUB), Money(0, EUR)), observe().first().map { it.balance })

        transactions.upsert(history[0])
        assertEquals(Money(70_000, RUB), observe().first().first { it.account == rubCard }.balance)
    }

    @Test
    fun `hides archived accounts unless asked`() = runTest {
        val accounts = FakeAccountRepository().apply { upsert(rubCard); upsert(eurCash.copy(archived = true)) }
        val observe = ObserveAccountsWithBalances(accounts, FakeTransactionRepository())
        assertEquals(listOf(rubCard), observe().first().map { it.account })
        assertEquals(2, observe(includeArchived = true).first().size)
    }
}

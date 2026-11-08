package app.coshell.core.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.RoomCategoryRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.domain.usecase.ObserveAccountsWithBalances
import app.coshell.core.model.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

class RoomRepositoriesTest {
    private val now = Instant.fromEpochMilliseconds(5_000)
    private val clock = object : Clock {
        override fun now() = now
    }
    private val db = Room.inMemoryDatabaseBuilder<CoShellDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    private val accounts = RoomAccountRepository(db.accountDao(), clock)
    private val categories = RoomCategoryRepository(db.categoryDao(), clock)
    private val transactions = RoomTransactionRepository(db.transactionDao(), clock)

    @AfterTest
    fun tearDown() = db.close()

    @Test
    fun `stores accounts and stamps the update time`() = runTest {
        accounts.upsert(card)
        assertEquals(card.copy(meta = card.meta.copy(updatedAt = now)), accounts.getAccount(card.id))
    }

    @Test
    fun `hides archived accounts unless asked`() = runTest {
        accounts.upsert(card)
        accounts.upsert(cash.copy(archived = true))
        assertEquals(listOf(card.id), accounts.observeAccounts().first().map { it.id })
        assertEquals(2, accounts.observeAccounts(includeArchived = true).first().size)
    }

    @Test
    fun `soft deleted rows disappear from reads but stay in the table`() = runTest {
        accounts.upsert(card)
        categories.upsert(food)
        transactions.upsert(coffee)

        accounts.delete(card.id)
        categories.delete(food.id)
        transactions.delete(coffee.id)

        assertNull(accounts.getAccount(card.id))
        assertEquals(emptyList(), categories.observeCategories().first())
        assertEquals(emptyList(), transactions.observeTransactions().first())
        assertEquals(1, db.transactionDao().countDeleted())
    }

    @Test
    fun `lists transactions newest first and by account on both transfer sides`() = runTest {
        transactions.upsert(exchange)
        transactions.upsert(coffee)
        assertEquals(listOf(coffee.id, exchange.id), transactions.observeTransactions().first().map { it.id })
        assertEquals(listOf(exchange.id), transactions.observeTransactions(cash.id).first().map { it.id })
        assertEquals(2, transactions.observeTransactions(card.id).first().size)
    }

    @Test
    fun `finds imported transactions by source and external id`() = runTest {
        transactions.upsert(coffee)
        assertEquals(coffee.id, transactions.findImported("wallet", "w-42")?.id)
        assertNull(transactions.findImported("wallet", "w-43"))
    }

    @Test
    fun `balances work end to end on the database`() = runTest {
        accounts.upsert(card)
        accounts.upsert(cash)
        transactions.upsert(coffee)
        transactions.upsert(exchange)

        val balances = ObserveAccountsWithBalances(accounts, transactions)().first()
            .associate { it.account.id to it.balance }
        assertEquals(Money(-500 - 35_000 - 1_000_000, RUB), balances[card.id])
        assertEquals(Money(10_000, EUR), balances[cash.id])
    }
}

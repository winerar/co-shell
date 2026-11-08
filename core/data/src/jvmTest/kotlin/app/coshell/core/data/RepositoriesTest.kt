package app.coshell.core.data

import app.cash.turbine.test
import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.RoomCategoryRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

class RepositoriesTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val fixedClock = object : Clock {
        override fun now() = Instant.parse("2026-11-08T12:00:00Z")
    }

    private val db: CoShellDatabase = createInMemoryCoShellDatabase()
    private val stamper = WriteStamper(fixedClock) { "member-1" }
    private val accounts = RoomAccountRepository(db.accountDao(), db.transactionDao(), stamper)
    private val categories = RoomCategoryRepository(db.categoryDao(), stamper)
    private val transactions = RoomTransactionRepository(db.transactionDao(), stamper)

    @AfterTest
    fun tearDown() = db.close()

    private fun day(d: Int) = Instant.parse("2026-11-${d.toString().padStart(2, '0')}T10:00:00Z")

    @Test
    fun `balance is initial balance plus incomes minus expenses and transfers`() = runTest {
        accounts.saveAccount(Account("usd", "Bybit", usd, initialBalanceMinor = 1000_00))
        accounts.saveAccount(Account("ars", "Песо", ars))
        transactions.saveTransaction(Transaction("e", TransactionType.EXPENSE, "usd", Money(13_00, usd), day(1)))
        transactions.saveTransaction(Transaction("i", TransactionType.INCOME, "usd", Money(50_00, usd), day(2)))
        transactions.saveTransaction(
            Transaction(
                "x", TransactionType.TRANSFER, "usd", Money(100_00, usd), day(3),
                toAccountId = "ars", toAmount = Money(156_800_00, ars),
            ),
        )
        transactions.saveTransaction(Transaction("e2", TransactionType.EXPENSE, "ars", Money(15_000_00, ars), day(4)))

        val balances = accounts.observeAccountsWithBalance().first().associate { it.account.id to it.balance }
        assertEquals(Money(937_00, usd), balances["usd"])
        assertEquals(Money(141_800_00, ars), balances["ars"])
    }

    @Test
    fun `balances update when a transaction is deleted`() = runTest {
        accounts.saveAccount(Account("usd", "Bybit", usd))
        accounts.observeAccountsWithBalance().test {
            assertEquals(Money(0, usd), awaitItem().single().balance)
            transactions.saveTransaction(Transaction("e", TransactionType.EXPENSE, "usd", Money(5_00, usd), day(1)))
            assertEquals(Money(-5_00, usd), awaitItem().single().balance)
            transactions.deleteTransaction("e")
            assertEquals(Money(0, usd), awaitItem().single().balance)
        }
    }

    @Test
    fun `deleted records are tombstones hidden from reads`() = runTest {
        accounts.saveAccount(Account("a", "A", usd))
        accounts.deleteAccount("a")
        assertNull(accounts.getAccount("a"))
        assertEquals(emptyList(), accounts.observeAccounts().first())
        val raw = db.accountDao().getAll()
        assertEquals(emptyList(), raw) // getAll hides tombstones too
    }

    @Test
    fun `archived accounts are hidden unless requested`() = runTest {
        accounts.saveAccount(Account("a", "A", usd))
        accounts.saveAccount(Account("b", "B", usd, archived = true))
        assertEquals(listOf("a"), accounts.observeAccounts().first().map { it.id })
        assertEquals(listOf("a", "b"), accounts.observeAccounts(includeArchived = true).first().map { it.id })
    }

    @Test
    fun `transaction round-trips every field`() = runTest {
        val tx = Transaction(
            id = "t",
            type = TransactionType.EXPENSE,
            accountId = "usd",
            amount = Money(13_00, usd),
            date = day(8),
            categoryId = "edu",
            note = "Испанский",
            payee = "Escuela",
            labels = listOf("испанский", "дети"),
            originalAmount = Money(15_000_00, ars),
            authorMemberId = "member-1",
            importSource = "wallet",
            importExternalId = "abc",
        )
        transactions.saveTransaction(tx)
        assertEquals(tx, transactions.getTransaction("t"))
        val entity = db.transactionDao().get("t")!!
        assertEquals(fixedClock.now().toEpochMilliseconds(), entity.updatedAt)
        assertEquals("member-1", entity.updatedBy)
        assertEquals(true, entity.pendingSync)
    }

    @Test
    fun `filters transactions by account, category and period, newest first`() = runTest {
        transactions.saveTransaction(Transaction("1", TransactionType.EXPENSE, "a", Money(1, usd), day(1), categoryId = "food"))
        transactions.saveTransaction(Transaction("2", TransactionType.EXPENSE, "b", Money(1, usd), day(2), categoryId = "food"))
        transactions.saveTransaction(
            Transaction("3", TransactionType.TRANSFER, "b", Money(1, usd), day(3), toAccountId = "a", toAmount = Money(1, usd)),
        )
        assertEquals(listOf("3", "2", "1"), transactions.observeTransactions().first().map { it.id })
        assertEquals(listOf("3", "1"), transactions.observeTransactions(TransactionFilter(accountId = "a")).first().map { it.id })
        assertEquals(listOf("2", "1"), transactions.observeTransactions(TransactionFilter(categoryId = "food")).first().map { it.id })
        assertEquals(
            listOf("2"),
            transactions.observeTransactions(TransactionFilter(from = day(2), to = day(3))).first().map { it.id },
        )
        assertEquals(2, transactions.countForAccount("a"))
    }

    @Test
    fun `the same imported record cannot be stored twice`() = runTest {
        fun imported(id: String) = Transaction(
            id, TransactionType.EXPENSE, "a", Money(1, usd), day(1), importSource = "wallet", importExternalId = "w1",
        )
        transactions.saveTransaction(imported("1"))
        // Room's @Upsert swallows the unique-index conflict: the second row is silently dropped.
        // The Wallet importer (stage 5) must therefore look up importExternalId before saving.
        transactions.saveTransaction(imported("2"))
        assertEquals(listOf("1"), transactions.observeTransactions().first().map { it.id })
    }

    @Test
    fun `categories are filtered by type and counted`() = runTest {
        categories.saveCategories(
            listOf(
                Category("food", "Продукты", CategoryType.EXPENSE),
                Category("salary", "Зарплата", CategoryType.INCOME),
            ),
        )
        assertEquals(listOf("food"), categories.observeCategories(CategoryType.EXPENSE).first().map { it.id })
        assertEquals(2, categories.countCategories())
        categories.deleteCategory("food")
        assertEquals(1, categories.countCategories())
    }
}

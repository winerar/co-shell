package app.coshell.core.data

import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.RoomImportRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.domain.importer.ImportWrite
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
import kotlin.time.Clock
import kotlin.time.Instant

class ImportRepositoryTest {
    private val usd = CurrencyCode("USD")
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-10T12:00:00Z")
    }
    private val db: CoShellDatabase = createInMemoryCoShellDatabase()
    private val stamper = WriteStamper(clock) { "member-1" }
    private val imports = RoomImportRepository(db.importDao(), stamper)
    private val accounts = RoomAccountRepository(db.accountDao(), db.transactionDao(), stamper)
    private val transactions = RoomTransactionRepository(db.transactionDao(), stamper)

    @AfterTest
    fun tearDown() = db.close()

    private fun op(ext: String, day: Int, batch: String) = Transaction(
        id = "t-$ext", type = TransactionType.EXPENSE, accountId = "a", amount = Money(100, usd),
        date = Instant.parse("2026-11-0${day}T10:00:00Z"), categoryId = "c",
        importSource = "wallet", importExternalId = ext, importBatchId = batch,
    )

    private fun write(batch: String, vararg ext: String) = ImportWrite(
        accounts = listOf(Account("a", "Карта", usd)),
        categories = listOf(Category("c", "Еда", CategoryType.EXPENSE)),
        transactions = ext.mapIndexed { i, e -> op(e, i + 1, batch) },
    )

    @Test
    fun `save writes accounts, categories and operations with the importer as author`() = runTest {
        imports.save(write("b1", "w1", "w2"))
        assertEquals(Money(-200, usd), accounts.observeAccountsWithBalance().first().single().balance)
        assertEquals("member-1", transactions.getTransaction("t-w1")?.authorMemberId)
        assertEquals(setOf("w1", "w2"), imports.knownExternalIds("wallet"))
        assertEquals(emptySet(), imports.knownExternalIds("other"))
    }

    @Test
    fun `batches are listed with their size and period`() = runTest {
        imports.save(write("b1", "w1", "w2", "w3"))
        val batch = imports.observeBatches().first().single()
        assertEquals("b1", batch.id)
        assertEquals("wallet", batch.source)
        assertEquals(3, batch.count)
        assertEquals(Instant.parse("2026-11-01T10:00:00Z"), batch.firstDate)
        assertEquals(Instant.parse("2026-11-03T10:00:00Z"), batch.lastDate)
        assertEquals(clock.now(), batch.importedAt)
    }

    @Test
    fun `undo removes the batch and the same file can be imported again`() = runTest {
        imports.save(write("b1", "w1", "w2"))
        transactions.saveTransaction(Transaction("manual", TransactionType.EXPENSE, "a", Money(5, usd), clock.now()))

        assertEquals(2, imports.undoBatch("b1"))
        assertEquals(listOf("manual"), transactions.observeTransactions().first().map { it.id })
        assertEquals(emptyList(), imports.observeBatches().first())
        assertEquals(emptySet(), imports.knownExternalIds("wallet"))

        // New row ids, same external ids: the freed unique keys accept them.
        val again = write("b2", "w1", "w2").let { w -> w.copy(transactions = w.transactions.map { it.copy(id = it.id + "-2") }) }
        imports.save(again)
        assertEquals(3, transactions.observeTransactions().first().size)
    }

    @Test
    fun `a deleted imported operation stays known and is not brought back`() = runTest {
        imports.save(write("b1", "w1"))
        transactions.deleteTransaction("t-w1")
        assertEquals(setOf("w1"), imports.knownExternalIds("wallet"))
    }
}

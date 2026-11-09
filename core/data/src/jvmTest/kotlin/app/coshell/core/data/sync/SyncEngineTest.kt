package app.coshell.core.data.sync

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Instant

class SyncEngineTest {
    private val usd = CurrencyCode("USD")
    private val server = FakeRemoteStore()
    private val a = Phone("member-a", server, startMs = 1_000_000)
    private val b = Phone("member-b", server, startMs = 1_000_000)

    @AfterTest
    fun tearDown() { a.close(); b.close() }

    private fun expense(id: String, minor: Long, note: String = "") =
        Transaction(id, TransactionType.EXPENSE, "acc", Money(minor, usd), Instant.parse("2026-11-09T10:00:00Z"), note = note)

    private suspend fun Phone.pending() = db.syncDao().observePendingCount().first()

    @Test
    fun `records created on one phone appear on the other`() = runTest {
        a.accounts.saveAccount(Account("acc", "Bybit", usd, color = 0xFFB388FF))
        a.transactions.saveTransaction(expense("t1", 12_50))

        assertEquals(2, a.engine.sync().pushed)
        assertEquals(0, a.pending())
        b.engine.sync()

        assertEquals("Bybit", b.accounts.getAccount("acc")?.name)
        assertEquals(0xFFB388FF, b.accounts.getAccount("acc")?.color, "the account colour syncs too")
        assertEquals(Money(12_50, usd), b.transactions.getTransaction("t1")?.amount)
        assertEquals(0, b.pending(), "rows from the server are not sent back")
    }

    @Test
    fun `later edit wins even if the earlier one reaches the server last`() = runTest {
        a.transactions.saveTransaction(expense("t1", 10_00))
        a.engine.sync(); b.engine.sync()

        // Both phones offline: A edits first, B edits a minute later.
        a.tick(); a.transactions.saveTransaction(expense("t1", 20_00, note = "from A"))
        b.tick(60_000); b.transactions.saveTransaction(expense("t1", 30_00, note = "from B"))

        b.engine.sync() // B's newer edit reaches the server first
        a.engine.sync() // A comes online later with an older edit
        b.engine.sync()

        assertEquals("from B", a.transactions.getTransaction("t1")?.note)
        assertEquals("from B", b.transactions.getTransaction("t1")?.note)
        assertEquals("from B", server.records.single { it.record.id == "t1" }.let { SyncCodec.transaction(it.record).note })
    }

    @Test
    fun `deletion travels as a tombstone`() = runTest {
        a.transactions.saveTransaction(expense("t1", 10_00))
        a.engine.sync(); b.engine.sync()

        a.tick(); a.transactions.deleteTransaction("t1")
        a.engine.sync(); b.engine.sync()

        assertNull(b.transactions.getTransaction("t1"))
        assertEquals(true, server.records.single().record.deleted)
    }

    @Test
    fun `edit made after a delete on the other phone brings the record back`() = runTest {
        a.transactions.saveTransaction(expense("t1", 10_00))
        a.engine.sync(); b.engine.sync()

        a.tick(); a.transactions.deleteTransaction("t1")
        b.tick(5_000); b.transactions.saveTransaction(expense("t1", 15_00))
        a.engine.sync(); b.engine.sync(); a.engine.sync()

        assertEquals(Money(15_00, usd), a.transactions.getTransaction("t1")?.amount)
    }

    @Test
    fun `failed push keeps rows pending and the next run sends them`() = runTest {
        a.transactions.saveTransaction(expense("t1", 10_00))
        server.failNextPush = true

        assertFailsWith<IllegalStateException> { a.engine.sync() }
        assertEquals(1, a.pending())

        a.engine.sync()
        assertEquals(0, a.pending())
        assertEquals(1, server.records.size)
    }

    @Test
    fun `failed pull changes nothing`() = runTest {
        a.transactions.saveTransaction(expense("t1", 10_00))
        a.engine.sync()
        server.failNextPull = true

        assertFailsWith<IllegalStateException> { b.engine.sync() }
        assertNull(b.transactions.getTransaction("t1"))
        b.engine.sync()
        assertEquals(Money(10_00, usd), b.transactions.getTransaction("t1")?.amount)
    }

    @Test
    fun `row edited while being sent stays pending`() = runTest {
        a.transactions.saveTransaction(expense("t1", 10_00))
        server.afterPush = {
            server.afterPush = {}
            a.tick(); a.transactions.saveTransaction(expense("t1", 99_00))
        }

        a.engine.sync()
        assertEquals(1, a.pending(), "the newer edit must still be sent")

        a.engine.sync()
        b.engine.sync()
        assertEquals(Money(99_00, usd), b.transactions.getTransaction("t1")?.amount)
    }

    @Test
    fun `second sync pulls only what is new`() = runTest {
        a.transactions.saveTransaction(expense("t1", 10_00))
        a.engine.sync()
        b.engine.sync()

        a.tick(); a.transactions.saveTransaction(expense("t2", 5_00))
        a.engine.sync()
        val result = b.engine.sync()

        // The cursor record (t1) is read again by design, but only t2 changes anything.
        assertEquals(2, result.pulled)
        assertEquals(1, result.applied)
        assertEquals(0, result.pushed)
    }
}

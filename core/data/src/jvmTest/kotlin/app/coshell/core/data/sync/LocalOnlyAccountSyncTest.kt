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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/** #558 «Только у меня»: a local-only account and everything on it never leaves the phone and is never overwritten. */
class LocalOnlyAccountSyncTest {
    private val usd = CurrencyCode("USD")
    private val server = FakeRemoteStore()
    private val a = Phone("member-a", server, startMs = 1_000_000)
    private val b = Phone("member-b", server, startMs = 1_000_000)

    @AfterTest
    fun tearDown() { a.close(); b.close() }

    private fun tx(id: String, account: String, to: String? = null) = Transaction(
        id, if (to == null) TransactionType.EXPENSE else TransactionType.TRANSFER, account, Money(10_00, usd),
        Instant.parse("2026-11-09T10:00:00Z"), toAccountId = to, toAmount = to?.let { Money(10_00, usd) },
    )

    @Test
    fun `local-only account and its operations are not sent`() = runTest {
        a.accounts.saveAccount(Account("shared", "Cash", usd))
        a.accounts.saveAccount(Account("mine", "Piggy", usd, localOnly = true))
        a.transactions.saveTransaction(tx("t-shared", "shared"))
        a.transactions.saveTransaction(tx("t-mine", "mine"))
        a.engine.sync()
        b.engine.sync()

        assertEquals(setOf("shared", "t-shared"), server.records.map { it.record.id }.toSet())
        assertNull(b.accounts.getAccount("mine"))
        assertNotNull(a.accounts.getAccount("mine"), "stays on the owner's phone")
        assertNotNull(a.transactions.getTransaction("t-mine"))
    }

    @Test
    fun `a transfer with a local-only leg stays on the phone`() = runTest {
        a.accounts.saveAccount(Account("shared", "Cash", usd))
        a.accounts.saveAccount(Account("mine", "Piggy", usd, localOnly = true))
        a.transactions.saveTransaction(tx("out", "shared", to = "mine"))
        a.transactions.saveTransaction(tx("back", "mine", to = "shared"))
        a.engine.sync()

        assertTrue(server.records.none { it.record.id == "out" || it.record.id == "back" })
    }

    @Test
    fun `pending count does not include local-only rows`() = runTest {
        a.accounts.saveAccount(Account("mine", "Piggy", usd, localOnly = true))
        a.transactions.saveTransaction(tx("t-mine", "mine"))
        assertEquals(0, a.db.syncDao().pending().size)
        assertEquals(0, a.db.syncDao().observePendingCount().first())
    }

    @Test
    fun `a record of the other phone does not overwrite a local-only account`() = runTest {
        a.accounts.saveAccount(Account("mine", "Piggy", usd, localOnly = true))
        // The same id arrives from the server (should never happen, but must not erase privacy).
        b.accounts.saveAccount(Account("mine", "Other", usd))
        b.tick(5_000_000); b.accounts.saveAccount(Account("mine", "Other 2", usd))
        b.engine.sync()
        a.tick(10_000_000)
        a.engine.sync()

        val mine = a.accounts.getAccount("mine")
        assertEquals("Piggy", mine?.name)
        assertTrue(mine?.localOnly == true)
    }

    @Test
    fun `turning the mark off sends the account and its operations`() = runTest {
        a.accounts.saveAccount(Account("mine", "Piggy", usd, localOnly = true))
        a.transactions.saveTransaction(tx("t-mine", "mine"))
        a.engine.sync()
        assertTrue(server.records.isEmpty())

        a.tick(); a.accounts.saveAccount(a.accounts.getAccount("mine")!!.copy(localOnly = false))
        a.engine.sync()
        b.engine.sync()

        assertNotNull(b.accounts.getAccount("mine"))
        assertNotNull(b.transactions.getTransaction("t-mine"))
    }

    @Test
    fun `replacing data with the family's keeps local-only accounts`() = runTest {
        a.accounts.saveAccount(Account("shared", "Cash", usd))
        a.accounts.saveAccount(Account("mine", "Piggy", usd, localOnly = true))
        a.transactions.saveTransaction(tx("t-shared", "shared"))
        a.transactions.saveTransaction(tx("t-mine", "mine"))
        a.db.syncDao().wipeSyncedData()

        assertNull(a.accounts.getAccount("shared"))
        assertNull(a.transactions.getTransaction("t-shared"))
        assertNotNull(a.accounts.getAccount("mine"))
        assertNotNull(a.transactions.getTransaction("t-mine"))
    }
}

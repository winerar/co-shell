package app.coshell.core.data.sync

import app.coshell.core.data.remote.ApiException
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/** #130: a big import that runs out of the Firestore daily write quota finishes the next day. */
class SyncQuotaTest {
    private val usd = CurrencyCode("USD")
    private val server = FakeRemoteStore()
    // 2026-11-11 15:00 UTC = 07:00 in Los Angeles; the quota resets at 08:00 UTC on Nov 12.
    private val a = Phone("member-a", server, startMs = Instant.parse("2026-11-11T15:00:00Z").toEpochMilliseconds())
    private val b = Phone("member-b", server, startMs = Instant.parse("2026-11-11T15:00:00Z").toEpochMilliseconds())

    @AfterTest
    fun tearDown() { a.close(); b.close() }

    private suspend fun Phone.pending() = db.syncDao().observePendingCount().first()

    private suspend fun Phone.addExpenses(count: Int) = repeat(count) {
        transactions.saveTransaction(
            Transaction("t$it", TransactionType.EXPENSE, "acc", Money(1_00, usd), Instant.parse("2026-11-01T10:00:00Z")),
        )
    }

    @Test
    fun `quota keeps what was sent and pauses sending until the reset`() = runTest {
        a.addExpenses(1_000)
        server.writesLeft = 500 // the first 400-row commit fits, the second does not

        val error = assertFailsWith<ApiException> { a.engine.sync() }
        assertTrue(error.isQuotaExceeded)
        assertEquals(400, server.records.size, "the commit that fitted stays on the server")
        assertEquals(600, a.pending(), "the rest waits")

        // Polls during the pause do not even try to send, but still receive the other phone's edits.
        val pushesBefore = server.pushCalls
        a.tick(10.hours.inWholeMilliseconds)
        val paused = a.engine.sync()
        assertTrue(paused.pushPaused)
        assertEquals(0, paused.pushed)
        assertEquals(pushesBefore, server.pushCalls)
        b.engine.sync()
        assertEquals(400, b.transactions.observeTransactions().first().size, "pulling goes on")

        // After the reset (Nov 12, 00:00 in Los Angeles) the rest goes out.
        server.writesLeft = 20_000
        a.nowMs = Instant.parse("2026-11-12T08:00:00Z").toEpochMilliseconds()
        val resumed = a.engine.sync()
        assertFalse(resumed.pushPaused)
        assertEquals(600, resumed.pushed)
        assertEquals(0, a.pending())
        assertEquals(1_000, server.records.size)
    }

    @Test
    fun `other server errors do not pause sending`() = runTest {
        a.addExpenses(3)
        server.failNextPush = true
        assertFailsWith<IllegalStateException> { a.engine.sync() }
        val next = a.engine.sync()
        assertFalse(next.pushPaused)
        assertEquals(3, next.pushed)
    }

    @Test
    fun `quota resets at midnight Pacific time, summer and winter`() {
        assertEquals(Instant.parse("2026-11-12T08:00:00Z"), QuotaPause.nextReset(Instant.parse("2026-11-11T15:00:00Z")))
        assertEquals(Instant.parse("2026-07-02T07:00:00Z"), QuotaPause.nextReset(Instant.parse("2026-07-01T07:00:00Z")))
        assertEquals(Instant.parse("2026-07-01T07:00:00Z"), QuotaPause.nextReset(Instant.parse("2026-07-01T06:59:00Z")))
    }
}

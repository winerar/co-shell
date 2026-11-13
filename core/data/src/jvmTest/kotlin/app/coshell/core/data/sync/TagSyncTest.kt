package app.coshell.core.data.sync

import app.coshell.core.data.remote.ApiException
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Tag
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class TagSyncTest {
    private val server = FakeRemoteStore()
    private val a = Phone("member-a", server, startMs = 1_000_000)
    private val b = Phone("member-b", server, startMs = 1_000_000)

    @AfterTest
    fun tearDown() { a.close(); b.close() }

    @Test
    fun `tags reach the other phone, deletes too`() = runTest {
        a.tags.saveTag(Tag("аня", "Аня", 0xFFFF4FD8))
        a.engine.sync(); b.engine.sync()
        assertEquals(listOf(Tag("аня", "Аня", 0xFFFF4FD8)), b.tags.getTags())
        assertEquals(listOf("tag_аня"), server.records.map { it.record.key })

        b.tick(); b.tags.deleteTag("аня")
        b.engine.sync(); a.engine.sync()
        assertEquals(emptyList(), a.tags.getTags())
    }

    @Test
    fun `the same imported label on both phones is one tag, and a rename wins over it`() = runTest {
        a.tags.addImportedTags(listOf(Tag("отпуск", "отпуск")))
        b.tags.addImportedTags(listOf(Tag("отпуск", "отпуск")))
        a.tick(); a.tags.saveTag(Tag("отпуск", "Отпуск 2026", 1))

        a.engine.sync(); b.engine.sync(); a.engine.sync()

        assertEquals(listOf(Tag("отпуск", "Отпуск 2026", 1)), a.tags.getTags())
        assertEquals(listOf(Tag("отпуск", "Отпуск 2026", 1)), b.tags.getTags())
        assertEquals(0, b.db.syncDao().observePendingCount().first())
    }

    @Test
    fun `old rules without the tag kind do not hold back the operations`() = runTest {
        server.rejectedKinds = setOf(SyncKind.TAG)
        a.tags.saveTag(Tag("аня", "Аня"))
        a.transactions.saveTransaction(
            Transaction(
                id = "t1", type = TransactionType.EXPENSE, accountId = "card", amount = Money(1_000_00, CurrencyCode("ARS")),
                date = Instant.fromEpochMilliseconds(1_000_000), labels = listOf("аня"),
            ),
        )

        val error = assertFailsWith<ApiException> { a.engine.sync() }
        assertEquals(true, error.isPermissionDenied)
        assertEquals(listOf("transaction_t1"), server.records.map { it.record.key })

        server.rejectedKinds = emptySet()
        a.engine.sync(); b.engine.sync()
        assertEquals(listOf(Tag("аня", "Аня")), b.tags.getTags())
    }
}

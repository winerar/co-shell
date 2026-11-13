package app.coshell.core.data

import app.coshell.core.data.backup.JsonBackupRepository
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomDataWipeRepository
import app.coshell.core.data.repository.RoomTagRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.domain.BackupImportResult
import app.coshell.core.domain.tags.TagUsage
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Tag
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/** Tags in Room (#129): repository, usage of `labels`, imported tags, backup, «Стереть всё». */
class TagDataTest {
    private val ars = CurrencyCode("ARS")

    private class Repos {
        val db = createInMemoryCoShellDatabase()
        val stamper = WriteStamper()
        val tags = RoomTagRepository(db.tagDao(), stamper)
        val transactions = RoomTransactionRepository(db.transactionDao(), stamper)
        val backup = JsonBackupRepository(db, stamper)
        val wipe = RoomDataWipeRepository(db.wipeDao(), stamper)
    }

    private fun op(id: String, day: Long, vararg labels: String) = Transaction(
        id, TransactionType.EXPENSE, "galicia", Money(100, ars), Instant.fromEpochMilliseconds(day), labels = labels.toList(),
    )

    @Test
    fun `tags are listed by name and deleted softly`() = runTest {
        val r = Repos()
        r.tags.saveTag(Tag("работа", "работа", 0xFF9D7BFF))
        r.tags.saveTag(Tag("аня", "Аня"))
        r.tags.saveTag(Tag("b", "Барилоче"))

        assertEquals(listOf("Аня", "Барилоче", "работа"), r.tags.observeTags().first().map { it.name })
        r.tags.deleteTag("b")
        assertNull(r.tags.getTag("b"))
        assertTrue(r.tags.isIdTaken("b"), "a tombstone keeps its id")
        assertEquals(3, r.db.syncDao().pendingTags().size, "the tombstone stays for sync")
    }

    @Test
    fun `usage counts live operations per resolved tag`() = runTest {
        val r = Repos()
        r.transactions.saveTransaction(op("1", 100, "Аня", "отпуск"))
        r.transactions.saveTransaction(op("2", 300, "аня ", "Аня"))
        r.transactions.saveTransaction(op("3", 500, "отпуск"))
        r.transactions.saveTransaction(op("4", 900))
        r.transactions.deleteTransaction("3")

        val usage = r.tags.observeUsage().first()
        assertEquals(setOf("аня", "отпуск"), usage.keys)
        assertEquals(TagUsage("Аня", 2, Instant.fromEpochMilliseconds(300)), usage["аня"]?.copy(label = "Аня"))
        assertEquals(1, usage.getValue("отпуск").count)
    }

    @Test
    fun `imported tags never overwrite and lose to any edit`() = runTest {
        val r = Repos()
        r.tags.saveTag(Tag("аня", "Анна", 1))
        r.tags.addImportedTags(listOf(Tag("аня", "Аня"), Tag("кот", "кот")))

        assertEquals(listOf(Tag("аня", "Анна", 1), Tag("кот", "кот")), r.tags.getTags().sortedBy { it.id })
        assertEquals(0L, r.db.syncDao().pendingTags().single { it.id == "кот" }.updatedAt)
    }

    @Test
    fun `backup carries tags and wipe removes them`() = runTest {
        val source = Repos()
        source.tags.saveTag(Tag("аня", "Аня", 0xFFFF4FD8))
        source.tags.saveTag(Tag("кот", "кот"))
        val json = source.backup.exportJson()

        val target = Repos()
        val result = assertIs<BackupImportResult.Imported>(target.backup.importJson(json))
        assertEquals(2, result.tags)
        assertEquals(source.tags.getTags().toSet(), target.tags.getTags().toSet())

        target.wipe.wipeAll()
        assertEquals(emptyList(), target.tags.getTags())
    }
}

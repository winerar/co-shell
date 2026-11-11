package app.coshell.core.data

import app.coshell.core.data.backup.JsonBackupRepository
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomCategoryRepository
import app.coshell.core.data.repository.RoomDataWipeRepository
import app.coshell.core.data.repository.RoomTemplateRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.domain.BackupImportResult
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/** Templates in Room: repository, backup file, «Стереть всё». */
class TemplateDataTest {
    private val ars = CurrencyCode("ARS")

    private class Repos {
        val db = createInMemoryCoShellDatabase()
        val stamper = WriteStamper()
        val templates = RoomTemplateRepository(db.templateDao(), stamper)
        val categories = RoomCategoryRepository(db.categoryDao(), stamper)
        val backup = JsonBackupRepository(db, stamper)
        val wipe = RoomDataWipeRepository(db.wipeDao(), stamper)
    }

    private val spanish = TransactionTemplate(
        "spanish", "Испанский", TransactionType.EXPENSE, "galicia", ars, Money(40_000_00, ars),
        categoryId = "edu", note = "Марта", sortOrder = 1,
    )
    private val coffee = TransactionTemplate("coffee", "Кофе", TransactionType.EXPENSE, "galicia", ars, sortOrder = 0)
    private val move = TransactionTemplate("move", "В наличные", TransactionType.TRANSFER, "galicia", ars, toAccountId = "cash", sortOrder = 2)

    @Test
    fun `templates are stored in their order and deleted softly`() = runTest {
        val r = Repos()
        assertNull(r.templates.maxSortOrder())
        listOf(spanish, move, coffee).forEach { r.templates.saveTemplate(it) }

        assertEquals(listOf(coffee, spanish, move), r.templates.observeTemplates().first())
        assertEquals(2, r.templates.maxSortOrder())

        r.templates.deleteTemplate("move")
        assertNull(r.templates.getTemplate("move"))
        assertEquals(1, r.templates.maxSortOrder())
        assertEquals(3, r.db.syncDao().pendingTemplates().size, "the tombstone stays for sync")
    }

    @Test
    fun `backup carries templates`() = runTest {
        val source = Repos()
        listOf(spanish, coffee, move).forEach { source.templates.saveTemplate(it) }
        source.templates.deleteTemplate("coffee")

        val target = Repos()
        val result = target.backup.importJson(source.backup.exportJson())

        assertEquals(2, assertIs<BackupImportResult.Imported>(result).templates)
        assertEquals(listOf(spanish, move), target.templates.observeTemplates().first())
    }

    @Test
    fun `backup made before templates imports and keeps local templates`() = runTest {
        val r = Repos()
        r.templates.saveTemplate(coffee)
        val old = """{"format":"co-shell-backup","version":1,"exportedAt":"2026-11-08T00:00:00Z","accounts":[]}"""

        assertEquals(0, assertIs<BackupImportResult.Imported>(r.backup.importJson(old)).templates)
        assertEquals(listOf(coffee), r.templates.observeTemplates().first())
    }

    @Test
    fun `broken template aborts the import`() = runTest {
        val r = Repos()
        val json = """{"format":"co-shell-backup","version":1,"exportedAt":"2026-11-08T00:00:00Z",
            "templates":[{"id":"t","name":" ","type":"EXPENSE","accountId":"a","currency":"ARS"}]}"""
        assertIs<BackupImportResult.Corrupted>(r.backup.importJson(json))
    }

    @Test
    fun `local template follows its category replaced by the one from the backup`() = runTest {
        val source = Repos()
        source.categories.saveCategory(Category("edu", "Образование", CategoryType.EXPENSE))
        val json = source.backup.exportJson()

        val target = Repos()
        target.categories.saveCategory(Category("seeded", "образование", CategoryType.EXPENSE))
        target.templates.saveTemplate(spanish.copy(categoryId = "seeded"))
        target.backup.importJson(json)

        assertEquals("edu", target.templates.getTemplate("spanish")?.categoryId)
    }

    @Test
    fun `wipe deletes templates too`() = runTest {
        val r = Repos()
        r.templates.saveTemplate(spanish)
        r.wipe.wipeAll()
        assertEquals(emptyList(), r.templates.observeTemplates().first())
        assertEquals(true, r.db.syncDao().pendingTemplates().single().deleted)
    }
}

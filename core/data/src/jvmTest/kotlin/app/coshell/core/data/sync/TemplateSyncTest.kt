package app.coshell.core.data.sync

import app.coshell.core.data.db.SyncStateEntity
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TemplateSyncTest {
    private val ars = CurrencyCode("ARS")
    private val server = FakeRemoteStore()
    private val a = Phone("member-a", server, startMs = 1_000_000)
    private val b = Phone("member-b", server, startMs = 1_000_000)
    private val coffee = TransactionTemplate("coffee", "Кофе", TransactionType.EXPENSE, "card", ars, categoryId = "cafe", sortOrder = 2)

    @AfterTest
    fun tearDown() { a.close(); b.close() }

    @Test
    fun `template travels to the other phone under its own kind`() = runTest {
        a.templates.saveTemplate(coffee)

        assertEquals(1, a.engine.sync().pushed)
        assertEquals(listOf("template_coffee"), server.records.map { it.record.key })
        b.engine.sync()

        assertEquals(listOf(coffee), b.templates.observeTemplates().first())
        assertEquals(0, b.db.syncDao().observePendingCount().first(), "rows from the server are not sent back")
    }

    @Test
    fun `later edit wins and deletion travels as a tombstone`() = runTest {
        a.templates.saveTemplate(coffee)
        a.engine.sync(); b.engine.sync()

        b.tick(); b.templates.saveTemplate(coffee.copy(amount = Money(3_500_00, ars), name = "Кофе в Havanna"))
        b.engine.sync(); a.engine.sync()
        assertEquals("Кофе в Havanna", a.templates.getTemplate("coffee")?.name)
        assertEquals(Money(3_500_00, ars), a.templates.getTemplate("coffee")?.amount)

        a.tick(2_000); a.templates.deleteTemplate("coffee")
        a.engine.sync(); b.engine.sync()
        assertNull(b.templates.getTemplate("coffee"))
    }

    @Test
    fun `phone that skipped an unknown kind reads everything again after the update`() = runTest {
        a.templates.saveTemplate(coffee)
        a.engine.sync()
        // B ran a version without templates: it moved its cursor past the record and kept no trace of it.
        val dao = b.db.syncDao()
        dao.setState(SyncStateEntity(SyncEngine.CURSOR, server.records.maxOf { it.cursor }))

        assertEquals(1, b.engine.sync().applied)
        assertEquals(coffee, b.templates.getTemplate("coffee"))

        val readBefore = server.recordsRead
        b.engine.sync()
        assertEquals(readBefore, server.recordsRead, "the full re-read happens once")
    }
}

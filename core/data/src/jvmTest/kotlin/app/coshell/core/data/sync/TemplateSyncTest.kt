package app.coshell.core.data.sync

import app.coshell.core.data.db.SyncStateEntity
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TemplateSyncTest {
    private val ars = CurrencyCode("ARS")
    private val server = FakeRemoteStore()
    private val a = Phone("member-a", server, startMs = 1_000_000)
    private val b = Phone("member-b", server, startMs = 1_000_000)
    private val coffee = TransactionTemplate("coffee", "Кофе", TransactionType.EXPENSE, "card", ars, categoryId = "cafe", sortOrder = 2,
        schedule = RecurringSchedule(Recurrence.Monthly(31), LocalDate(2026, 11, 1)),
    )

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
    fun `payment in another currency travels with both currencies and reads safely for an older version`() = runTest {
        val rent = TransactionTemplate("rent", "Аренда", TransactionType.EXPENSE, "card", ars, Money(650_00, CurrencyCode("USD")))
        a.templates.saveTemplate(rent)
        a.engine.sync(); b.engine.sync()

        assertEquals(rent, b.templates.getTemplate("rent"))
        val json = server.records.single().record.json
        // An older version reads only `currency`: 650 USD on an ARS account — the account no longer fits, nothing in ARS.
        assertTrue("\"currency\":\"USD\"" in json, json)
        assertTrue("\"accountCurrency\":\"ARS\"" in json, json)
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

    @Test
    fun `edit on another phone does not bring a posted payment back`() = runTest {
        a.templates.saveTemplate(coffee)
        a.engine.sync(); b.engine.sync()
        // A posts November; B renames the template before it has seen that.
        a.tick(); a.templates.saveTemplate(coffee.copy(schedule = coffee.schedule?.copy(handledThrough = LocalDate(2026, 11, 30))))
        b.tick(2_000); b.templates.saveTemplate(coffee.copy(name = "Кофе в Havanna"))

        a.engine.sync(); b.engine.sync(); a.engine.sync(); b.engine.sync()

        for (phone in listOf(a, b)) {
            val t = phone.templates.getTemplate("coffee")
            assertEquals("Кофе в Havanna", t?.name, phone.toString())
            assertEquals(LocalDate(2026, 11, 30), t?.schedule?.handledThrough, phone.toString())
            assertEquals(0, phone.db.syncDao().observePendingCount().first(), "converged, nothing left to send")
        }
    }

    @Test
    fun `later handled date keeps the winner rule and skips unreadable schedules`() {
        val winner = "M5;2026-12-01;REMIND;"
        assertEquals("M5;2026-12-01;REMIND;2026-11-30", TemplateHandledMerge.laterHandled(winner, "M31;2026-11-01;REMIND;2026-11-30"))
        assertEquals("M5;2026-12-01;REMIND;2026-12-05", TemplateHandledMerge.laterHandled("M5;2026-12-01;REMIND;2026-12-05", "M31;2026-11-01;REMIND;2026-11-30"))
        assertEquals(winner, TemplateHandledMerge.laterHandled(winner, "Z9;future"))
        assertEquals(null, TemplateHandledMerge.laterHandled(null, "M31;2026-11-01;REMIND;2026-11-30"), "«no repeat» stays")
        assertEquals("Q1;x", TemplateHandledMerge.laterHandled("Q1;x", "M31;2026-11-01;REMIND;2026-11-30"), "a newer kind is left as is")
    }
}

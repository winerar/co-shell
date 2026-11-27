package app.coshell.core.data

import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomGoalRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.data.sync.FakeRemoteStore
import app.coshell.core.data.sync.Phone
import app.coshell.core.data.sync.SyncCodec
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Goal
import app.coshell.core.model.Money
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Savings goals in Room (#768): save, soft delete, sync between phones, an old payload without new fields. */
class GoalDataTest {
    private val usd = CurrencyCode("USD")
    private val trip = Goal("g1", "Отпуск", Money(2_000_00, usd), "box", LocalDate(2027, 7, 1), icon = "gift")

    @Test
    fun `a goal is saved, changed and deleted softly`() = runTest {
        val db = createInMemoryCoShellDatabase()
        val goals = RoomGoalRepository(db.goalDao(), WriteStamper())
        goals.save(trip)
        assertEquals(listOf(trip), goals.observe().first())
        goals.save(trip.copy(name = "Отпуск в Барилоче"))
        assertEquals("Отпуск в Барилоче", goals.get("g1")?.name)

        goals.delete("g1")
        assertEquals(emptyList(), goals.observe().first())
        assertNull(goals.get("g1"))
        assertEquals(listOf(true), db.syncDao().pending().goals.map { it.deleted })
        db.close()
    }

    @Test
    fun `goals reach the other phone and a deletion follows`() = runTest {
        val server = FakeRemoteStore()
        val a = Phone("member-a", server, startMs = 1_000_000)
        val b = Phone("member-b", server, startMs = 1_000_000)
        a.goals.save(trip)
        a.engine.sync(); b.engine.sync()
        assertEquals(listOf(trip), b.goals.observe().first())

        b.tick(); b.goals.save(trip.copy(target = Money(2_500_00, usd)))
        b.engine.sync(); a.engine.sync()
        assertEquals(Money(2_500_00, usd), a.goals.get("g1")?.target)

        a.tick(5_000); a.goals.delete("g1")
        a.engine.sync(); b.engine.sync()
        assertEquals(emptyList(), b.goals.observe().first())
        a.close(); b.close()
    }

    @Test
    fun `a record without the optional fields reads as a one-off goal`() {
        val json = """{"id":"g2","name":"Велосипед","targetMinor":45000,"currency":"USD","accountId":"box","updatedAt":1,"updatedBy":"x"}"""
        val row = SyncCodec.goal(app.coshell.core.data.sync.SyncRecord(app.coshell.core.data.sync.SyncKind.GOAL, "g2", 1, "x", false, json))
        assertEquals(null, row.deadline)
        assertEquals(false, row.yearly)
    }
}

package app.coshell.core.data

import app.coshell.core.data.backup.JsonBackupRepository
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomDataWipeRepository
import app.coshell.core.data.repository.RoomDebtRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.data.sync.FakeRemoteStore
import app.coshell.core.data.sync.Phone
import app.coshell.core.domain.BackupImportResult
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.DebtDirection
import app.coshell.core.model.DebtEvent
import app.coshell.core.model.DebtEventKind
import app.coshell.core.model.Money
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

/** Debts and loans in Room (#552): balance, sync between phones, backup round trip, «Стереть всё». */
class DebtDataTest {
    private val ars = CurrencyCode("ARS")
    private val day = Instant.fromEpochMilliseconds(86_400_000L * 20_000)

    private fun issue(id: String, debt: String, dir: DebtDirection, minor: Long, account: String = "galicia") =
        DebtEvent(id, debt, DebtEventKind.ISSUE, dir, Money(minor, ars), day, account, "Аня", LocalDate(2026, 12, 1), "на аренду")

    private fun back(id: String, debt: String, dir: DebtDirection, minor: Long, account: String = "galicia") =
        DebtEvent(id, debt, DebtEventKind.RETURN, dir, Money(minor, ars), day, account)

    @Test
    fun `a loan takes money out of the account and a return brings it back`() = runTest {
        val db = createInMemoryCoShellDatabase()
        val debts = RoomDebtRepository(db.debtDao(), WriteStamper())
        debts.add(listOf(issue("e1", "d1", DebtDirection.LENT, 1_000), back("e2", "d1", DebtDirection.LENT, 400)))
        debts.add(listOf(issue("e3", "d2", DebtDirection.BORROWED, 5_000, "cash")))

        val deltas = db.transactionDao().observeBalanceDeltas().first().groupBy { it.accountId }.mapValues { (_, v) -> v.sumOf { it.deltaMinor } }
        assertEquals(mapOf("galicia" to -600L, "cash" to 5_000L), deltas)

        debts.deleteDebt("d1")
        val after = db.transactionDao().observeBalanceDeltas().first().groupBy { it.accountId }.mapValues { (_, v) -> v.sumOf { it.deltaMinor } }
        assertEquals(mapOf("cash" to 5_000L), after)
        assertEquals(listOf("e3"), debts.events().map { it.id })
        db.close()
    }

    @Test
    fun `events reach the other phone and a deleted debt disappears there`() = runTest {
        val server = FakeRemoteStore()
        val a = Phone("member-a", server, startMs = 1_000_000)
        val b = Phone("member-b", server, startMs = 1_000_000)
        a.debts.add(listOf(issue("e1", "d1", DebtDirection.LENT, 1_000)))
        a.engine.sync(); b.engine.sync()
        assertEquals(listOf(issue("e1", "d1", DebtDirection.LENT, 1_000)), b.debts.events())

        b.tick(); b.debts.add(listOf(back("e2", "d1", DebtDirection.LENT, 300)))
        b.engine.sync(); a.engine.sync()
        assertEquals(listOf("e1", "e2"), a.debts.events().map { it.id })

        a.tick(5_000); a.debts.deleteDebt("d1")
        a.engine.sync(); b.engine.sync()
        assertEquals(emptyList(), b.debts.events())
        a.close(); b.close()
    }

    @Test
    fun `backup and wipe keep the circle closed`() = runTest {
        val db = createInMemoryCoShellDatabase()
        val stamper = WriteStamper()
        val debts = RoomDebtRepository(db.debtDao(), stamper)
        val backup = JsonBackupRepository(db, stamper)
        val wipe = RoomDataWipeRepository(db.wipeDao(), stamper)
        val events = listOf(issue("e1", "d1", DebtDirection.BORROWED, 2_000), back("e2", "d1", DebtDirection.BORROWED, 500))
        debts.add(events)

        val file = backup.exportJson()
        wipe.wipeAll()
        assertEquals(emptyList(), debts.events())

        assertIs<BackupImportResult.Imported>(backup.importJson(file))
        assertEquals(events, debts.events())
        assertEquals(file.substringAfter("exportedAt").substringAfter(','), backup.exportJson().substringAfter("exportedAt").substringAfter(','))
        db.close()
    }
}

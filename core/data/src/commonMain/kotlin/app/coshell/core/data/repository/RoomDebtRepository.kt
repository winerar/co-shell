package app.coshell.core.data.repository

import app.coshell.core.data.db.DebtDao
import app.coshell.core.domain.debts.DebtRepository
import app.coshell.core.model.DebtEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Debts and loans (#552) in Room: events are added or soft-deleted, never edited. */
class RoomDebtRepository(
    private val debts: DebtDao,
    private val stamper: WriteStamper,
) : DebtRepository {

    override fun observeEvents(): Flow<List<DebtEvent>> = debts.observe().map { list -> list.map { it.toModel() } }

    override suspend fun events(): List<DebtEvent> = debts.getAll().map { it.toModel() }

    override suspend fun add(events: List<DebtEvent>) {
        val stamp = stamper.stamp()
        debts.upsert(events.map { it.toEntity(stamp) })
    }

    override suspend fun deleteDebt(debtId: String) {
        val stamp = stamper.stamp()
        debts.markDebtDeleted(debtId, stamp.now, stamp.by)
    }

    override suspend fun deleteEvent(id: String) {
        val stamp = stamper.stamp()
        debts.markDeleted(id, stamp.now, stamp.by)
    }
}

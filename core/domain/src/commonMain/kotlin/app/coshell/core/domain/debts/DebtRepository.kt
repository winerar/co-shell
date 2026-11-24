package app.coshell.core.domain.debts

import app.coshell.core.model.DebtEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Where the history of debts lives (#552): events are added and soft-deleted, never edited (`DebtEvent`). */
interface DebtRepository {
    fun observeEvents(): Flow<List<DebtEvent>>

    suspend fun events(): List<DebtEvent>

    suspend fun add(events: List<DebtEvent>)

    /** Every event of the debt: a debt entered by mistake. Balances come back, as if it had never been. */
    suspend fun deleteDebt(debtId: String)

    /** One event, e.g. a return typed wrongly. */
    suspend fun deleteEvent(id: String)
}

/** For tests and previews. */
class InMemoryDebtRepository : DebtRepository {
    private val all = MutableStateFlow<List<DebtEvent>>(emptyList())
    override fun observeEvents(): Flow<List<DebtEvent>> = all.map { list -> list.sortedBy { it.date } }
    override suspend fun events(): List<DebtEvent> = all.value
    override suspend fun add(events: List<DebtEvent>) {
        all.value = all.value.filter { old -> events.none { it.id == old.id } } + events
    }
    override suspend fun deleteDebt(debtId: String) {
        all.value = all.value.filter { it.debtId != debtId }
    }
    override suspend fun deleteEvent(id: String) {
        all.value = all.value.filter { it.id != id }
    }
}

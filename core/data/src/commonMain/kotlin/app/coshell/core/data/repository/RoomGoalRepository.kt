package app.coshell.core.data.repository

import app.coshell.core.data.db.GoalDao
import app.coshell.core.domain.goals.GoalRepository
import app.coshell.core.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Savings goals (#768) in Room; deleting is soft (a tombstone for sync). */
class RoomGoalRepository(
    private val goals: GoalDao,
    private val stamper: WriteStamper,
) : GoalRepository {

    override fun observe(): Flow<List<Goal>> = goals.observe().map { list -> list.map { it.toModel() } }

    override suspend fun get(id: String): Goal? = goals.get(id)?.toModel()

    override suspend fun save(goal: Goal) {
        goals.upsert(listOf(goal.toEntity(stamper.stamp())))
    }

    override suspend fun delete(id: String) {
        val stamp = stamper.stamp()
        goals.markDeleted(id, stamp.now, stamp.by)
    }
}

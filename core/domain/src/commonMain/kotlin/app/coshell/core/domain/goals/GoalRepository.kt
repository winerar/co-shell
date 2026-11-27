package app.coshell.core.domain.goals

import app.coshell.core.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Where savings goals live (#768). */
interface GoalRepository {
    fun observe(): Flow<List<Goal>>

    suspend fun get(id: String): Goal?

    suspend fun save(goal: Goal)

    suspend fun delete(id: String)
}

/** For tests and previews. */
class InMemoryGoalRepository(initial: List<Goal> = emptyList()) : GoalRepository {
    private val all = MutableStateFlow(initial)
    override fun observe(): Flow<List<Goal>> = all.map { list -> list.sortedBy { it.name.lowercase() } }
    override suspend fun get(id: String): Goal? = all.value.firstOrNull { it.id == id }
    override suspend fun save(goal: Goal) {
        all.value = all.value.filter { it.id != goal.id } + goal
    }
    override suspend fun delete(id: String) {
        all.value = all.value.filter { it.id != id }
    }
}

package app.coshell.core.domain.budgets

import app.coshell.core.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    /** All live limit records of all months; pick the ones of a month with [inMonth]. */
    fun observeBudgets(): Flow<List<Budget>>
    suspend fun getBudgets(): List<Budget>

    /** Saves [save] and deletes [deleteIds] in one transaction. */
    suspend fun applyBudgets(save: List<Budget> = emptyList(), deleteIds: List<String> = emptyList())
}

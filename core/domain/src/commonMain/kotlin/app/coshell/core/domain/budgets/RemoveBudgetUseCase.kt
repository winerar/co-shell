package app.coshell.core.domain.budgets

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus

/**
 * Removes the limit of a category "from this month on": records that start at [month] or later are
 * deleted, an earlier one that still runs is ended at the previous month, so past months keep their plan.
 */
class RemoveBudgetUseCase(private val budgets: BudgetRepository) {

    suspend operator fun invoke(categoryId: String, month: YearMonth) {
        val records = budgets.getBudgets().filter { it.categoryId == categoryId }
        val previousMonth = month.minus(1, DateTimeUnit.MONTH)
        val ended = records
            .filter { it.startMonth < month && it.covers(month) }
            .map { it.copy(lastMonth = previousMonth) }
        val deleted = records.filter { it.startMonth >= month }.map { it.id }
        if (ended.isNotEmpty() || deleted.isNotEmpty()) budgets.applyBudgets(save = ended, deleteIds = deleted)
    }
}

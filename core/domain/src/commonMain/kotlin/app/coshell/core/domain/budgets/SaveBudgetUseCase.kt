package app.coshell.core.domain.budgets

import app.coshell.core.domain.CategoryRepository
import app.coshell.core.model.Budget
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.budgetId
import kotlinx.datetime.YearMonth

/**
 * Sets the limit of a category or a folder "from this month on". Earlier months keep their limit:
 * a record that started before [month] stays, a new one starts at [month]. A limit that already
 * starts at [month] is replaced. A category inside a folder may have its own limit next to the folder's.
 */
class SaveBudgetUseCase(
    private val categories: CategoryRepository,
    private val budgets: BudgetRepository,
) {
    sealed interface Result {
        data class Saved(val budget: Budget) : Result
        data class Invalid(val reason: Reason) : Result
    }

    enum class Reason { CATEGORY_NOT_FOUND, NOT_EXPENSE, SYSTEM_CATEGORY, AMOUNT_NOT_POSITIVE }

    suspend operator fun invoke(
        categoryId: String,
        month: YearMonth,
        limit: Money,
        note: String? = null,
        rollover: Boolean = false,
    ): Result {
        if (SystemCategories.adjustmentType(categoryId) != null) return Result.Invalid(Reason.SYSTEM_CATEGORY)
        val category = categories.getCategory(categoryId)?.takeUnless { it.archived }
            ?: return Result.Invalid(Reason.CATEGORY_NOT_FOUND)
        if (category.type != CategoryType.EXPENSE) return Result.Invalid(Reason.NOT_EXPENSE)
        if (!limit.isPositive) return Result.Invalid(Reason.AMOUNT_NOT_POSITIVE)

        val records = budgets.getBudgets().filter { it.categoryId == categoryId }
        val budget = Budget(
            id = budgetId(categoryId, month),
            categoryId = categoryId,
            startMonth = month,
            limit = limit,
            note = note?.trim()?.takeIf { it.isNotEmpty() },
            rollover = rollover,
        )
        // Records of the same start month under another id (made before ids became stable) would compete.
        val replaced = records.filter { it.startMonth == month && it.id != budget.id }.map { it.id }
        budgets.applyBudgets(save = listOf(budget), deleteIds = replaced)
        return Result.Saved(budget)
    }
}

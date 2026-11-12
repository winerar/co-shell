package app.coshell.budgets.detail

import app.coshell.budgets.detail.BudgetDetailUiState.Head
import app.coshell.budgets.detail.BudgetDetailUiState.Inner
import app.coshell.budgets.list.buildEnvelope
import app.coshell.budgets.list.folderSizes
import app.coshell.budgets.list.number
import app.coshell.core.domain.budgets.BudgetLedger
import app.coshell.core.domain.budgets.BudgetProgress
import app.coshell.core.model.Category
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.LocalDate

/**
 * Pure: the head and the categories of the envelope on [categoryId]. [progress] — every limit of the month
 * (the calculator's), [ledger] — spending of the categories that have no limit of their own, in the folder's
 * currency. Categories keep the order of the «Категории» screen; archived ones only when they have spending.
 */
internal fun buildEnvelopeDetail(
    categoryId: String,
    progress: List<BudgetProgress>,
    categories: List<Category>,
    ledger: BudgetLedger,
    today: LocalDate,
    format: MoneyFormat,
): Pair<Head?, List<Inner>> {
    val item = progress.singleOrNull { it.budget.categoryId == categoryId } ?: return null to emptyList()
    val byId = categories.associateBy { it.id }
    val head = Head(
        envelope = buildEnvelope(item, byId[categoryId], folderSizes(categories)[categoryId], format),
        left = format.format(item.left.abs()),
        over = item.left.isNegative,
        daysLeft = if (today in item.period) item.period.daysLeft(today) else null,
        paceToday = item.paceToday?.let { format.number(it) },
    )
    val limits = progress.associateBy { it.budget.categoryId }
    val currency = item.budget.limit.currency
    val inner = categories.filter { it.parentId == categoryId }.sortedBy { it.sortOrder }.mapNotNull { category ->
        limits[category.id]?.let { return@mapNotNull Inner.Limited(buildEnvelope(it, category, null, format)) }
        val spent = ledger.spent(setOf(category.id), item.period, currency).sum
        if (category.archived && spent.isZero) return@mapNotNull null
        Inner.Plain(category.id, category.name, category.icon, category.color, spent.takeUnless { it.isZero }?.let(format::format))
    }
    return head to inner
}

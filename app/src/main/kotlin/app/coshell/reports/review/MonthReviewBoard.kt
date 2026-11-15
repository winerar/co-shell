package app.coshell.reports.review

import app.coshell.budgets.BudgetSource
import app.coshell.budgets.list.buildEnvelopes
import app.coshell.budgets.list.number
import app.coshell.core.designsystem.charts.BulletTone
import app.coshell.core.domain.budgets.BudgetProgress
import app.coshell.core.domain.reports.CategoryChange
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.monthReview
import app.coshell.core.model.Category
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.reports.review.MonthReviewUiState.Change
import app.coshell.reports.review.MonthReviewUiState.Limits
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * Pure: «Итог месяца» of [month] from the budget data — the report numbers (`monthReview`, the rules of all reports) in
 * the main report currency, and the month's limits as «Бюджеты» counts them. [today] — for the limits' carry-over.
 */
internal fun buildMonthReview(snapshot: BudgetSource.Snapshot, month: YearMonth, today: LocalDate, format: MoneyFormat): MonthReviewUiState {
    val categories = snapshot.categories
    val ledger = ReportLedger(snapshot.days, categories, snapshot.book, snapshot.mainCurrency)
    val progress = if (snapshot.budgets.isEmpty()) emptyList() else snapshot.calculator().month(month, today)
    val review = monthReview(ledger, categories, snapshot.period(month), snapshot.startDay, progress)
    val byId = categories.associateBy { it.id }
    return MonthReviewUiState(
        month = month,
        startDay = snapshot.startDay,
        currency = snapshot.mainCurrency.value,
        saved = format.number(review.saved),
        overspent = review.saved.isNegative,
        savingsRate = review.savingsRate,
        spent = format.number(review.spent),
        earned = format.number(review.earned),
        usualMonths = review.usualMonths,
        changes = review.changes.map { buildChange(it, byId[it.categoryId], format) },
        limits = review.limits?.let { buildLimits(progress, categories, format, overOrder = it.over.map { p -> p.budget.categoryId }) },
        missing = review.missing.map { it.value }.sorted(),
        // A month with nothing in it shows no card: «everything −100%» says only that the app was not used.
        isEmpty = !review.spent.isPositive && !review.earned.isPositive,
    )
}

private fun buildChange(change: CategoryChange, category: Category?, format: MoneyFormat): Change {
    val difference = change.difference
    return Change(
        categoryId = change.categoryId,
        name = category?.name,
        icon = category?.icon,
        color = category?.color,
        amount = format.number(change.amount),
        usual = format.number(change.usual),
        difference = signed(difference, format),
        percent = change.percent,
        more = difference.isPositive,
    )
}

private fun buildLimits(
    progress: List<BudgetProgress>,
    categories: List<Category>,
    format: MoneyFormat,
    overOrder: List<String>,
): Limits {
    val all = buildEnvelopes(progress, categories, format).flatMap { listOf(it.copy(children = emptyList())) + it.children }
    val over = all.filter { it.tone == BulletTone.DANGER }.sortedBy { overOrder.indexOf(it.categoryId) }
    val rest = all.filter { it.tone != BulletTone.DANGER }
    return Limits(within = rest.size, over = over, all = over + rest)
}

/** «+90» / «-75». */
private fun signed(money: Money, format: MoneyFormat): String =
    if (money.isPositive) "+" + format.number(money) else format.number(money)

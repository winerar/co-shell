package app.coshell.budgets.list

import app.coshell.budgets.list.BudgetsUiState.Caption
import app.coshell.budgets.list.BudgetsUiState.Envelope
import app.coshell.core.designsystem.components.BulletTone
import app.coshell.core.domain.budgets.BudgetMonthSummary
import app.coshell.core.domain.budgets.BudgetProgress
import app.coshell.core.domain.budgets.BudgetState
import app.coshell.core.domain.budgets.PACE_TOLERANCE_PERCENT
import app.coshell.core.model.Category
import app.coshell.core.model.Currencies
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.LocalDate

/**
 * Pure: the envelopes of a month from the calculator's [progress]. A limit on a category inside a folder
 * that has its own limit goes under the folder; otherwise it is a row of its own. Rows that need a look come
 * first (over the limit, then ahead of pace), untouched ones last, the rest by name.
 */
internal fun buildEnvelopes(progress: List<BudgetProgress>, categories: List<Category>, format: MoneyFormat): List<Envelope> {
    val byId = categories.associateBy { it.id }
    val limited = progress.map { it.budget.categoryId }.toSet()
    val folderSizes = categories.filter { it.parentId != null && !it.archived }.groupingBy { it.parentId!! }.eachCount()
    val (inner, top) = progress.partition { byId[it.budget.categoryId]?.parentId in limited }
    val innerByFolder = inner.groupBy { byId[it.budget.categoryId]?.parentId }
    return top.map { item ->
        val id = item.budget.categoryId
        envelope(item, byId[id], folderSizes[id], format, innerByFolder[id].orEmpty().map { envelope(it, byId[it.budget.categoryId], null, format) }.sorted())
    }.sorted()
}

/** Counts for the filter chips: every envelope, inner ones too. */
internal fun envelopeCounts(envelopes: List<Envelope>): Map<BudgetFilter, Int> {
    val all = envelopes.flatMap { listOf(it) + it.children }
    return BudgetFilter.entries.associateWith { filter -> all.count { it.matches(filter) } }
}

/** The month total in one currency; spent and pace give the same tone rules as one limit (`BudgetProgress`). */
internal fun buildSummary(summary: BudgetMonthSummary, today: LocalDate, format: MoneyFormat): BudgetsUiState.Summary {
    val period = summary.period
    val current = today in period
    val pace = if (current) period.daysPassed(today).toFloat() / period.days else null
    val fraction = fraction(summary.spent, summary.planned)
    val tone = when {
        summary.spent > summary.planned -> BulletTone.DANGER
        summary.spent.isZero -> BulletTone.IDLE
        pace != null && fraction > pace * (100 + PACE_TOLERANCE_PERCENT) / 100f -> BulletTone.WARNING
        else -> BulletTone.NORMAL
    }
    return BudgetsUiState.Summary(
        left = format.format(summary.left.abs()),
        over = summary.left.isNegative,
        spent = format.number(summary.spent),
        planned = format.format(summary.planned),
        fraction = fraction,
        pace = pace,
        tone = tone,
        daysLeft = if (current) period.daysLeft(today) else null,
        forecast = summary.forecast?.let { forecast ->
            BudgetsUiState.Forecast(format.format(forecast), (forecast - summary.planned).takeIf { it.isPositive }?.let(format::format))
        },
        missing = summary.missing.map { it.value }.sorted(),
    )
}

private fun envelope(item: BudgetProgress, category: Category?, folderSize: Int?, format: MoneyFormat, children: List<Envelope> = emptyList()): Envelope {
    val tone = tone(item)
    return Envelope(
        categoryId = item.budget.categoryId,
        // A category deleted on the other phone while its limit synced: still shown, so the limit can be removed.
        name = category?.name ?: "—",
        icon = category?.icon,
        color = category?.color,
        folderSize = folderSize,
        spent = format.number(item.spent),
        limit = format.format(item.available),
        fraction = fraction(item.spent, item.available),
        pace = item.paceToday?.let { pace -> if (item.available.isPositive) fraction(pace, item.available) else null },
        tone = tone,
        caption = caption(item, tone, format),
        carried = item.carried.takeUnless { it.isZero }?.let { (if (it.isPositive) "+" else "−") + format.format(it.abs()) },
        hasNote = item.budget.note != null,
        children = children,
        missing = item.missing.map { it.value }.sorted(),
    )
}

private fun tone(item: BudgetProgress): BulletTone = when (item.state) {
    BudgetState.OVER -> BulletTone.DANGER
    BudgetState.FAST -> BulletTone.WARNING
    BudgetState.OK -> if (item.spent.isZero) BulletTone.IDLE else BulletTone.NORMAL
}

private fun caption(item: BudgetProgress, tone: BulletTone, format: MoneyFormat): Caption = when (tone) {
    BulletTone.DANGER -> Caption.Over(format.format(item.left.abs()))
    BulletTone.WARNING -> Caption.Ahead(format.format(item.left), item.daysLeft)
    BulletTone.IDLE -> Caption.Idle
    BulletTone.NORMAL -> Caption.Left(format.format(item.left), item.perDay?.let(format::format))
}

/** Spent over the limit, 0…∞ (the bar caps it); a limit that is zero or less after carrying over is full. */
private fun fraction(spent: Money, of: Money): Float = when {
    of.isPositive -> spent.amountMinor.toFloat() / of.amountMinor
    spent.isPositive -> 1f
    else -> 0f
}

/** «310 000» — the number without the code, as in «310 000 / 400 000 ARS». */
private fun MoneyFormat.number(money: Money): String =
    formatNumber(money.amountMinor, Currencies.info(money.currency).fractionDigits)

private val severity = listOf(BulletTone.DANGER, BulletTone.WARNING, BulletTone.NORMAL, BulletTone.IDLE)

private fun List<Envelope>.sorted(): List<Envelope> =
    sortedWith(compareBy<Envelope>({ severity.indexOf(it.tone) }, { it.name.lowercase() }))

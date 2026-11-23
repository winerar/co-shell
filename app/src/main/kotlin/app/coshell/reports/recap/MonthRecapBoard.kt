package app.coshell.reports.recap

import app.coshell.budgets.BudgetSource
import app.coshell.budgets.list.number
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.reports.MonthRecap
import app.coshell.core.domain.reports.RecapPhrase
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.limitsTally
import app.coshell.core.domain.reports.monthRecap
import app.coshell.core.model.Category
import app.coshell.core.model.MoneyFormat
import app.coshell.reports.recap.MonthRecapUiState.NamedCategory
import kotlin.math.abs
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus

/** Pure: the recap of [month] from the budget data, counted by `monthRecap` in the main report currency. */
internal fun buildMonthRecap(snapshot: BudgetSource.Snapshot, month: YearMonth, today: LocalDate, format: MoneyFormat): MonthRecapUiState {
    val categories = snapshot.categories
    val ledger = ReportLedger(snapshot.days, categories, snapshot.book, snapshot.mainCurrency)
    val progress = if (snapshot.budgets.isEmpty()) emptyList() else snapshot.calculator().month(month, today)
    val period = snapshot.period(month)
    val before = budgetPeriod(month.minus(1, DateTimeUnit.MONTH), snapshot.startDay)
    val recap = monthRecap(ledger, categories, period, before, limitsTally(progress))
    if (!recap.spent.isPositive) return MonthRecapUiState(month = month, currency = snapshot.mainCurrency.value)
    val byId = categories.associateBy { it.id }
    val busiest = recap.weeks.maxOf { it.amountMinor }.coerceAtLeast(1L)
    return MonthRecapUiState(
        month = month,
        currency = snapshot.mainCurrency.value,
        spent = format.number(recap.spent),
        cards = recap.cards,
        top = recap.top.map { MonthRecapUiState.Share(named(it.categoryId, byId), format.number(it.amount), it.percent, it.amount.amountMinor) },
        previousSpent = recap.previousSpent?.let { format.number(it) },
        changePercent = recap.changePercent,
        weeks = recap.weeks.mapIndexed { i, w ->
            val from = i * WEEK + 1
            MonthRecapUiState.Week("$from–${minOf(from + WEEK - 1, period.days)}", format.number(w), w.amountMinor.toFloat() / busiest)
        },
        busiestWeek = recap.busiestWeek,
        newCategory = recap.newCategoryIds.firstOrNull()?.let { named(it, byId) },
        weekdayAverage = recap.weekdayAverage?.let { format.number(it) },
        weekendAverage = recap.weekendAverage?.let { format.number(it) },
        weekendMore = weekendMore(recap),
        weekendRatio = weekendRatio(recap),
        phrase = recap.phrase,
        phraseNumber = phraseNumber(recap),
        recordedDays = recap.recordedDays,
    )
}

private fun phraseNumber(recap: MonthRecap): Int = when (recap.phrase) {
    RecapPhrase.LIGHTER -> abs(recap.changePercent ?: 0)
    RecapPhrase.SAVED -> recap.savingsRate ?: 0
    RecapPhrase.STREAK -> recap.streak
    else -> 0
}

private fun named(id: String, byId: Map<String, Category>): NamedCategory {
    val category = byId[id]
    return NamedCategory(id, category?.name, category?.color ?: CategoryPalette.forKey(id))
}

private const val WEEK = 7

private fun weekendMore(recap: MonthRecap): Boolean =
    (recap.weekendAverage?.amountMinor ?: 0L) > (recap.weekdayAverage?.amountMinor ?: 0L)

private fun weekendRatio(recap: MonthRecap): Float {
    val weekday = recap.weekdayAverage?.amountMinor ?: return 1f
    val weekend = recap.weekendAverage?.amountMinor ?: return 1f
    val big = maxOf(weekday, weekend)
    return if (big <= 0L) 1f else minOf(weekday, weekend).toFloat() / big
}

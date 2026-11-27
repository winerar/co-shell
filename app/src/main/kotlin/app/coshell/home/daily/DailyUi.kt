package app.coshell.home.daily

import androidx.compose.runtime.Immutable
import app.coshell.core.domain.budgets.DailyBase
import app.coshell.core.domain.budgets.DailyPlan
import app.coshell.core.model.MoneyFormat
import app.coshell.ui.format.formatMonthOf

/** «Сегодня можно» on «Обзоре» (#769, board `DailyHome`) and its «Как посчитано» sheet (`DailyWhy`), everything formatted. */
@Immutable
data class DailyUi(
    val perDay: String,
    val days: Int,
    /** What is left for the month; when [over] — how much over the plan, without a sign. */
    val left: String,
    val over: Boolean,
    val spentToday: String,
    /** Today's spending against today's allowance, 0..1. */
    val todayFraction: Float,
    val base: DailyBase,
    /** «ноябрь». */
    val month: String,
    val baseAmount: String,
    val spent: String,
    /** Empty — the line is not shown. */
    val upcoming: String,
    val goals: String,
    val missing: String,
)

internal fun dailyUiOf(plan: DailyPlan, format: MoneyFormat): DailyUi {
    val a = plan.allowance
    val per = a.perDay.amountMinor
    return DailyUi(
        perDay = format.format(a.perDay),
        days = a.days,
        left = format.format(a.left.abs()),
        over = a.over,
        spentToday = format.format(a.spentToday),
        todayFraction = if (per <= 0) (if (a.spentToday.isPositive) 1f else 0f) else (a.spentToday.amountMinor.toFloat() / per).coerceIn(0f, 1f),
        base = plan.base,
        month = formatMonthOf(plan.period.month),
        baseAmount = format.format(plan.baseAmount),
        spent = format.format(plan.spent),
        upcoming = plan.upcoming.takeIf { it.isPositive }?.let(format::format).orEmpty(),
        goals = plan.goals.takeIf { it.isPositive }?.let(format::format).orEmpty(),
        missing = plan.missing.joinToString(", ") { it.value },
    )
}

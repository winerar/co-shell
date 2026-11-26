package app.coshell.goals

import androidx.compose.runtime.Immutable
import app.coshell.core.domain.goals.GoalLine
import app.coshell.core.domain.rates.BalanceTotal
import app.coshell.core.model.MoneyFormat
import app.coshell.ui.format.formatDayMonth
import app.coshell.ui.format.formatDayMonthYear

/** How the second line of a goal reads (#768): the words come from resources, the numbers are here. */
enum class GoalWhen { REACHED, OVERDUE, NO_DEADLINE, DEADLINE, YEARLY }

/** One goal line, everything formatted (board `GoalListA`). */
@Immutable
data class GoalRowUi(
    val id: String,
    val name: String,
    val icon: String?,
    val color: Long?,
    /** «1 250 USD» — saved. */
    val saved: String,
    /** «2 000 USD» — the target. */
    val target: String,
    /** «750 USD» — still to save. */
    val left: String,
    val fraction: Float,
    val whenKind: GoalWhen,
    /** «1 июля» (this year) or «1 июля 2028». */
    val deadline: String,
    /** «125 USD» a month, empty when there is nothing to put aside. */
    val perMonth: String,
    val accountName: String,
)

/** The hero of «Цели»: totals in the main currency, «≈ 350 USD» a month, «без учёта: ARS». */
@Immutable
data class GoalTotalsUi(val saved: String, val perMonth: String, val missing: String)

internal fun goalRowsOf(snapshot: GoalsSnapshot, format: MoneyFormat): List<GoalRowUi> = snapshot.lines.map { line -> line.toRow(snapshot, format) }

private fun GoalLine.toRow(snapshot: GoalsSnapshot, format: MoneyFormat): GoalRowUi {
    val p = progress
    val day = p.deadline
    val kind = when {
        p.reached -> GoalWhen.REACHED
        p.overdue -> GoalWhen.OVERDUE
        day == null -> GoalWhen.NO_DEADLINE
        goal.yearly -> GoalWhen.YEARLY
        else -> GoalWhen.DEADLINE
    }
    val target = goal.target.amountMinor
    return GoalRowUi(
        id = goal.id,
        name = goal.name,
        icon = goal.icon,
        color = goal.color,
        saved = format.format(p.saved),
        target = format.format(goal.target),
        left = format.format(p.left),
        fraction = (p.saved.amountMinor.toFloat() / target).coerceIn(0f, 1f),
        whenKind = kind,
        deadline = day?.let { if (it.year == snapshot.today.year) formatDayMonth(it) else formatDayMonthYear(it) }.orEmpty(),
        perMonth = p.perMonth?.let(format::format).orEmpty(),
        accountName = snapshot.accounts[goal.accountId]?.name.orEmpty(),
    )
}

internal fun goalTotalsOf(snapshot: GoalsSnapshot, format: MoneyFormat): GoalTotalsUi = GoalTotalsUi(
    saved = format.format(snapshot.saved.sum),
    perMonth = if (snapshot.perMonth.sum.amountMinor > 0) format.format(snapshot.perMonth.sum) else "",
    missing = missingOf(snapshot.saved, snapshot.perMonth),
)

private fun missingOf(vararg totals: BalanceTotal): String =
    totals.flatMap { it.missing }.distinct().joinToString(", ") { it.value }

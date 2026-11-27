package app.coshell.goals

import androidx.compose.runtime.Immutable
import app.coshell.core.domain.goals.GoalLine
import app.coshell.core.domain.goals.WHAT_IF_DEFAULT_SHARE
import app.coshell.core.domain.goals.WHAT_IF_SHARES
import app.coshell.core.domain.goals.goalWhatIf
import app.coshell.core.domain.goals.savingPace
import app.coshell.core.domain.goals.whatIfCategories
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.model.MoneyFormat
import app.coshell.ui.format.formatMonthYear
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * «Что если» of one goal (#792, boards `Smart792Line`, `Smart792Sheet`), every choice of the sheet counted ahead: [results] by
 * [key] of a category and a share; a missing one — that saving changes nothing by a whole month. [line] — the card's sentence.
 */
@Immutable
data class GoalWhatIfUi(
    val goalId: String,
    val goalName: String,
    val target: String,
    val categories: List<WhatIfOption>,
    val results: Map<String, WhatIfResult>,
    val line: WhatIfResult,
) {
    companion object {
        fun key(categoryId: String, share: Int) = "$categoryId:$share"
    }
}

/** A chip of the sheet: [name] `null` — a category deleted on another phone. */
@Immutable
data class WhatIfOption(val id: String, val name: String?)

/**
 * One answer: [saved] — «≈ 90 USD» a month; [now] — «Июль 2027», `null` when the goal does not get closer now; [with] — the month
 * with the saving; [earlier] — whole months sooner (`null` with [now] `null`); [months] — months to the goal with the saving.
 */
@Immutable
data class WhatIfResult(
    val categoryId: String,
    val categoryName: String?,
    val share: Int,
    val saved: String,
    val now: String?,
    val with: String,
    val earlier: Int?,
    val months: Int,
)

/** Pure: «Что если» of each goal of [lines] that is not reached; goals where no saving helps are left out. */
internal fun goalWhatIfs(lines: List<GoalLine>, data: WhatIfData, accounts: Map<String, app.coshell.core.model.Account>, today: LocalDate, format: MoneyFormat): Map<String, GoalWhatIfUi> {
    val names = data.categories.associate { it.id to it.name }
    return lines.filter { !it.progress.reached }.mapNotNull { line ->
        val goal = line.goal
        val currency = goal.target.currency
        val account = accounts[goal.accountId]?.takeIf { it.currency == currency } ?: return@mapNotNull null
        val pace = savingPace(data.balanceDays, account.id, currency, today)
        val options = whatIfCategories(ReportLedger(data.days, data.categories, data.book, currency), data.categories, data.months, data.scheduled)
        val results = options.flatMap { option ->
            WHAT_IF_SHARES.mapNotNull { share ->
                goalWhatIf(line.progress.left, pace, option, share, today)?.let { w ->
                    GoalWhatIfUi.key(option.categoryId, share) to WhatIfResult(
                        categoryId = option.categoryId,
                        categoryName = names[option.categoryId],
                        share = share,
                        saved = format.format(w.saved),
                        now = w.dateNow?.let { formatMonthYear(YearMonth(it.year, it.month)) },
                        with = formatMonthYear(YearMonth(w.dateWith.year, w.dateWith.month)),
                        earlier = w.monthsEarlier,
                        months = w.monthsWith,
                    )
                }
            }
        }.toMap()
        val first = options.firstNotNullOfOrNull { results[GoalWhatIfUi.key(it.categoryId, WHAT_IF_DEFAULT_SHARE)] } ?: return@mapNotNull null
        goal.id to GoalWhatIfUi(goal.id, goal.name, format.format(goal.target), options.map { WhatIfOption(it.categoryId, names[it.categoryId]) }, results, first)
    }.toMap()
}

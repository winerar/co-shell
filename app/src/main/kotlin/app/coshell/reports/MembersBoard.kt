package app.coshell.reports

import app.coshell.budgets.list.number
import app.coshell.core.designsystem.charts.ChartScale
import app.coshell.core.domain.reports.spendingByMember
import app.coshell.core.model.MoneyFormat
import app.coshell.reports.ReportsUiState.MemberRow
import app.coshell.reports.ReportsUiState.TopFolder

/**
 * Pure: «Кто сколько потратил» (#189) of the period — a row per member who spent, largest first, on one scale.
 * [others] — the other family members by id with their names; empty — no card.
 */
internal fun buildMembers(input: ReportInput, others: Map<String, String>, format: MoneyFormat): List<MemberRow> {
    val period = input.period
    val rows = spendingByMember(input.ledger, input.categories, others.keys, period.start, period.endExclusive)
    val scale = ChartScale.of(rows.map { it.amount.amountMinor })
    val byId = input.categories.associateBy { it.id }
    return rows.map { row ->
        MemberRow(
            memberId = row.memberId,
            name = row.memberId?.let { others[it] },
            value = format.number(row.amount),
            percent = row.percent,
            fraction = scale.of(row.amount.amountMinor),
            top = row.top.map { (id, money) -> TopFolder(byId[id]?.name, format.number(money)) },
        )
    }
}

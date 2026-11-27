package app.coshell.reports

import app.coshell.budgets.list.number
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.BalanceDay
import app.coshell.core.domain.reports.NET_WORTH_MONTHS
import app.coshell.core.domain.reports.NetWorth
import app.coshell.core.domain.reports.monthlySeries
import app.coshell.core.domain.reports.netWorth
import app.coshell.core.domain.reports.reportMonths
import app.coshell.core.model.Account
import app.coshell.core.model.MoneyFormat
import app.coshell.reports.ReportsUiState.Capital
import app.coshell.reports.ReportsUiState.CapitalPoint
import app.coshell.reports.ReportsUiState.WhyRow

/**
 * Pure: «Капитал» (#189) — a year of month ends up to the period's last month; «Почему изменился» for the months the
 * ledger holds (those of «По месяцам»), newest first. `null` — no account in the total or no month started.
 */
internal fun buildCapital(input: ReportInput, accounts: List<Account>, days: List<BalanceDay>, book: RateBook, format: MoneyFormat): Capital? =
    capitalWorth(input, accounts, days, book)?.let { buildCapital(it, format) }

/** The money at each month end of «Капитал» — shared with «Подушка» (#241). */
internal fun capitalWorth(input: ReportInput, accounts: List<Account>, days: List<BalanceDay>, book: RateBook): NetWorth? {
    val periods = reportMonths(input.period.lastMonth, NET_WORTH_MONTHS, input.startDay)
    val saved = monthlySeries(input.ledger, input.columns).associate { it.period to it.saved }
    return netWorth(accounts, days, periods, book, input.ledger.currency, input.today, saved)
}

internal fun buildCapital(worth: NetWorth, format: MoneyFormat): Capital =
    Capital(
        value = format.number(worth.now),
        change = format.signed(worth.change),
        falling = worth.change.isNegative,
        percent = worth.percent,
        months = worth.points.size,
        points = worth.points.map { CapitalPoint(it.period.month, format.number(it.amount), it.amount.amountMinor) },
        why = worth.changes.reversed().map {
            WhyRow(
                month = it.period.month,
                running = it.running,
                saved = format.signed(it.saved),
                savedNegative = it.saved.isNegative,
                rates = format.signed(it.other),
                total = format.signed(it.total),
                totalNegative = it.total.isNegative,
            )
        },
        missing = worth.missing.map { it.value }.sorted(),
    )

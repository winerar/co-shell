package app.coshell.reports

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.BalanceDay
import app.coshell.core.domain.reports.BalanceForecast
import app.coshell.core.domain.reports.BalanceForecasts
import app.coshell.core.domain.reports.HISTORY_DAYS
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.accountsAt
import app.coshell.core.model.Account
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import app.coshell.reports.ReportsUiState.Forecast
import app.coshell.reports.ReportsUiState.ForecastView
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** Days behind today the solid part of the line shows. */
internal const val FORECAST_PAST_DAYS = 14

/** The two horizons of the toggle «30 · 60 дней». */
internal val ForecastHorizons = intArrayOf(30, 60)

/**
 * Pure: «Прогноз остатка» (#770, board `Forecast770`) of the money counted into the total, in the report currency. [history] is
 * the ledger of the last [HISTORY_DAYS] days before today ([hasHistory] — there were operations in it). `null` — no rate for
 * some account, or nothing to forecast from (no template with «Повтор» and no history).
 */
internal fun buildForecast(
    today: LocalDate,
    accounts: List<Account>,
    balanceDays: List<BalanceDay>,
    templates: List<TransactionTemplate>,
    history: ReportLedger,
    hasHistory: Boolean,
    book: RateBook,
    format: MoneyFormat,
): Forecast? {
    val currency = history.currency
    val hasScheduled = templates.any { it.schedule != null && it.amount != null && it.type != TransactionType.TRANSFER }
    if (!hasScheduled && !hasHistory) return null
    fun totalAt(day: LocalDate): Long? {
        val worth = accountsAt(accounts, balanceDays, book, currency, day, null)
        return if (worth.any { it.converted == null }) null else worth.sumOf { it.converted?.amountMinor ?: 0L }
    }
    val start = totalAt(today) ?: return null
    val converter = CurrencyConverter(book)
    val last = ForecastHorizons.max()
    val (flows, missing) = BalanceForecasts.scheduledFlows(templates, converter, currency, today, today, today.plusDays(last))
    val from = today.minus(HISTORY_DAYS, DateTimeUnit.DAY)
    val net = history.earned(from, today).total(currency) - history.spent(from, today).total(currency)
    val pastNet = BalanceForecasts.pastScheduledNet(templates, converter, currency, today, from, today.minus(1, DateTimeUnit.DAY))
    // No history at all: «what the templates already did» would be taken off nothing and invent an income — drift 0.
    val daily = if (hasHistory) BalanceForecasts.variableDaily(net.amountMinor, pastNet) else 0L
    val past = (FORECAST_PAST_DAYS downTo 1).map { totalAt(today.minus(it, DateTimeUnit.DAY)) }
    val behind = if (past.any { it == null }) emptyList() else past.filterNotNull()
    return Forecast(
        ForecastHorizons.map { days ->
            BalanceForecasts.of(currency, start, today, days, flows.filter { it.day <= today.plusDays(days) }, daily, missing)
                .toView(behind, format)
        },
    )
}

private fun LocalDate.plusDays(days: Int): LocalDate = this.minus(-days, DateTimeUnit.DAY)

private fun BalanceForecast.toView(behind: List<Long>, format: MoneyFormat): ForecastView {
    val short = firstBelowZero != null
    return ForecastView(
        past = behind,
        future = points.map { it.balanceMinor },
        lowest = points.indexOf(lowest),
        warning = short,
        firstDay = points.first().day.minus(behind.size, DateTimeUnit.DAY),
        lastDay = points.last().day,
        lowDay = lowest.day,
        lowAmount = format.format(Money(lowest.balanceMinor, currency)),
        belowFrom = firstBelowZero,
        shortfall = format.format(Money(deepestShortfallMinor, currency)).takeIf { short },
        missing = missing.map { it.value }.sorted(),
    )
}

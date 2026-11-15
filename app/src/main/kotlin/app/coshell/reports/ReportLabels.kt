package app.coshell.reports

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.reports.ReportPeriodKind
import app.coshell.reports.ReportsUiState.Comparison
import app.coshell.ui.format.formatMonth
import app.coshell.ui.format.formatMonthYear
import app.coshell.ui.format.formatShortDate

/** «Ноябрь 2026», «4-й квартал 2026», «2026», «1 нояб. 2026 – 10 нояб. 2026». */
@Composable
internal fun periodTitle(period: ReportPeriod): String = when (period.kind) {
    ReportPeriodKind.MONTH -> formatMonthYear(period.firstMonth)
    ReportPeriodKind.QUARTER -> stringResource(R.string.reports_quarter_title, period.quarter, period.firstMonth.year)
    ReportPeriodKind.YEAR -> period.firstMonth.year.toString()
    ReportPeriodKind.CUSTOM -> stringResource(R.string.reports_range, formatShortDate(period.start), formatShortDate(period.lastDay))
}

/** «черта — октябрь» for a month, «черта — прошлый период» otherwise. */
@Composable
internal fun ghostCaption(period: ReportPeriod, startDay: Int): String =
    if (period.kind == ReportPeriodKind.MONTH) stringResource(R.string.reports_ghost_month, formatMonth(period.previous(startDay).firstMonth.firstDay))
    else stringResource(R.string.reports_ghost_other)

/** «за 11 дней · в прошлом месяце к 11-му было 820» or «в прошлом месяце — 820». */
@Composable
internal fun comparisonText(comparison: Comparison): String = when (comparison) {
    is Comparison.SoFar -> {
        val days = pluralStringResource(R.plurals.reports_days, comparison.days, comparison.days)
        val previous = when (comparison.kind) {
            ReportPeriodKind.MONTH -> stringResource(R.string.reports_so_far_month, comparison.previousDay, comparison.previous)
            ReportPeriodKind.QUARTER -> stringResource(R.string.reports_so_far_quarter, comparison.previous)
            ReportPeriodKind.YEAR -> stringResource(R.string.reports_so_far_year, comparison.previous)
            ReportPeriodKind.CUSTOM -> stringResource(R.string.reports_so_far_custom, comparison.previous)
        }
        "$days · $previous"
    }
    is Comparison.Whole -> when (comparison.kind) {
        ReportPeriodKind.MONTH -> stringResource(R.string.reports_whole_month, comparison.previous)
        ReportPeriodKind.QUARTER -> stringResource(R.string.reports_whole_quarter, comparison.previous)
        ReportPeriodKind.YEAR -> stringResource(R.string.reports_whole_year, comparison.previous)
        ReportPeriodKind.CUSTOM -> stringResource(R.string.reports_whole_custom, comparison.previous)
    }
}

/** Spending more than last time — the line is in the warning colour, less — in the calm one. */
internal val Comparison.isMore: Boolean
    get() = when (this) {
        is Comparison.SoFar -> faster
        is Comparison.Whole -> more
    }

@Composable
internal fun categoryName(name: String?): String = name ?: stringResource(R.string.reports_deleted_category)

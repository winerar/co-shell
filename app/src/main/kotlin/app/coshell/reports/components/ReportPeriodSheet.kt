package app.coshell.reports.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellChoicePill
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellLabelGrid
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.CoShellSwitchLine
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.reports.ReportPeriodKind
import app.coshell.reports.periodTitle
import app.coshell.ui.form.DateRangeSheet
import app.coshell.ui.format.formatMonthShort
import app.coshell.ui.sheet.ModalSheet
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** What «Показать» applies. */
data class ReportChoice(val period: ReportPeriod, val currency: String, val compare: Boolean)

/**
 * «Период и валюта» (board `RepPeriod`): month / quarter / year / own days, the report currency among the totals
 * currencies, «Сравнивать с прошлым периодом». Nothing changes until «Показать».
 */
@Composable
internal fun ReportPeriodSheet(
    initial: ReportChoice,
    currencies: List<String>,
    startDay: Int,
    today: LocalDate,
    onApply: (ReportChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    var kind by rememberSaveable { mutableStateOf(initial.period.kind) }
    // The year the month and quarter grids show; months and quarters remember their choice across kinds.
    var year by rememberSaveable { mutableIntStateOf(initial.period.firstMonth.year) }
    var month by rememberSaveable { mutableIntStateOf(initial.period.firstMonth.month.ordinal) }
    var custom by rememberSaveable { mutableStateOf(initial.period.takeIf { it.kind == ReportPeriodKind.CUSTOM }?.let { it.start.toString() to it.lastDay.toString() }) }
    var currency by rememberSaveable { mutableStateOf(initial.currency) }
    var compare by rememberSaveable { mutableStateOf(initial.compare) }
    var pickingDays by rememberSaveable { mutableStateOf(false) }

    val chosenMonth = YearMonth(year, month + 1)
    val period: ReportPeriod? = when (kind) {
        ReportPeriodKind.MONTH -> ReportPeriod.month(chosenMonth, startDay)
        ReportPeriodKind.QUARTER -> ReportPeriod.quarter(chosenMonth, startDay)
        ReportPeriodKind.YEAR -> ReportPeriod.year(year, startDay)
        ReportPeriodKind.CUSTOM -> custom?.let { (a, b) -> ReportPeriod.custom(LocalDate.parse(a), LocalDate.parse(b)) }
    }

    ModalSheet(
        title = stringResource(R.string.reports_period_title),
        onClosed = onDismiss,
        actionLabel = stringResource(R.string.reports_show),
        actionEnabled = period != null,
        onAction = { close -> period?.let { onApply(ReportChoice(it, currency, compare)) }; close() },
    ) {
        val kinds = ReportPeriodKind.entries
        CoShellSegmented(
            labels = listOf(R.string.reports_kind_month, R.string.reports_kind_quarter, R.string.reports_kind_year, R.string.reports_kind_custom).map { stringResource(it) },
            selected = kinds.indexOf(kind),
            onSelect = { kind = kinds[it] },
        )
        when (kind) {
            ReportPeriodKind.MONTH -> {
                YearStepper(year, onChange = { year = it })
                CoShellLabelGrid((1..12).map { formatMonthShort(YearMonth(year, it)) }, month, onSelect = { month = it })
            }
            ReportPeriodKind.QUARTER -> {
                YearStepper(year, onChange = { year = it })
                val labels = (0 until 4).map { q ->
                    stringResource(R.string.reports_range, formatMonthShort(YearMonth(year, q * 3 + 1)), formatMonthShort(YearMonth(year, q * 3 + 3)))
                }
                CoShellLabelGrid(labels, month / 3, onSelect = { month = it * 3 }, columns = 2)
            }
            ReportPeriodKind.YEAR -> {
                val years = (today.year - YEARS_SHOWN + 1..today.year).toList()
                CoShellLabelGrid(years.map { it.toString() }, years.indexOf(year), onSelect = { year = years[it] })
            }
            ReportPeriodKind.CUSTOM -> CoShellFilterChip(
                selected = period != null,
                onClick = { pickingDays = true },
                label = { Text(period?.let { periodTitle(it) } ?: stringResource(R.string.reports_pick_days)) },
                leadingIcon = {
                    Icon(painterResource(DesignR.drawable.ic_calendar_today), contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
                },
            )
        }
        CurrencyChoice(currencies, currency, onPick = { currency = it })
        CoShellSwitchLine(stringResource(R.string.reports_compare), stringResource(R.string.reports_compare_hint), compare, onCheckedChange = { compare = it })
    }
    if (pickingDays) {
        val current = custom?.let { (a, b) -> LocalDate.parse(a) to LocalDate.parse(b) }
        DateRangeSheet(
            first = current?.first,
            last = current?.second,
            onDismiss = { pickingDays = false },
            onPicked = { first, last ->
                custom = first.toString() to last.toString()
                pickingDays = false
            },
        )
    }
}

@Composable
private fun YearStepper(year: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        CoShellIconButton(onClick = { onChange(year - 1) }) {
            Icon(painterResource(DesignR.drawable.ic_chevron_right), stringResource(R.string.reports_previous), Modifier.rotate(180f))
        }
        Text(year.toString(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        CoShellIconButton(onClick = { onChange(year + 1) }) {
            Icon(painterResource(DesignR.drawable.ic_chevron_right), stringResource(R.string.reports_next))
        }
    }
}

@Composable
private fun ColumnScope.CurrencyChoice(currencies: List<String>, chosen: String, onPick: (String) -> Unit) {
    if (currencies.size < 2) return
    Text(stringResource(R.string.reports_currency_label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    currencies.chunked(CURRENCIES_IN_ROW).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            row.forEach { code -> CoShellChoicePill(code, code == chosen, onClick = { onPick(code) }) }
        }
    }
}

/** Years in «Год»: the last few — a family's history in the app is short. */
private const val YEARS_SHOWN = 8
private const val CURRENCIES_IN_ROW = 5

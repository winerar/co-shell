package app.coshell.transactions.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.search.PeriodRange
import app.coshell.core.domain.search.PeriodRange.Preset
import app.coshell.transactions.list.FilterLabels
import app.coshell.transactions.list.ListFilter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import app.coshell.core.designsystem.R as DesignR

/**
 * «Период» (board `Fb141APeriod`): ready-made periods, «Раньше» / «Позже» moving the chosen one by its own
 * length (a month, three months, a year, own days), and own days from a calendar.
 */
@Composable
internal fun PeriodSheet(
    filter: ListFilter,
    today: LocalDate,
    count: Int?,
    onFilter: ((ListFilter) -> ListFilter) -> Unit,
    onClosed: () -> Unit,
) {
    val period = filter.period
    FilterSheet(
        title = stringResource(R.string.filter_period_title),
        count = count,
        onReset = { onFilter { it.copy(period = null) } }.takeIf { period != null },
        onClosed = onClosed,
    ) { PeriodChoices(filter, today, onFilter) }
}

/** Inside of [PeriodSheet]; apart for the screenshot test. */
@Composable
internal fun ColumnScope.PeriodChoices(filter: ListFilter, today: LocalDate, onFilter: ((ListFilter) -> ListFilter) -> Unit) {
    var pickerOpen by rememberSaveable { mutableStateOf(false) }
    val period = filter.period
    val lit = period?.presetOn(today)
    val locale = LocalConfiguration.current.locales[0]
    val presets: List<Pair<Preset?, String>> = listOf(
        Preset.THIS_MONTH to stringResource(R.string.filter_period_this),
        Preset.LAST_MONTH to stringResource(R.string.filter_period_last),
        Preset.THREE_MONTHS to stringResource(R.string.filter_period_three_months),
        Preset.THIS_YEAR to stringResource(R.string.filter_period_this_year),
        Preset.LAST_YEAR to stringResource(R.string.filter_period_last_year),
        null to stringResource(R.string.filter_period_all),
    )
    presets.chunked(2).forEach { pair ->
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            pair.forEach { (preset, label) ->
                CoShellFilterChip(
                    selected = if (preset == null) period == null else lit == preset,
                    onClick = { onFilter { it.copy(period = preset?.range(today)) } },
                    label = { Text(label, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    if (period != null) {
        Stepper(
            label = stepperLabel(period, today, locale),
            onEarlier = { onFilter { f -> f.copy(period = f.period?.shifted(-1)) } },
            onLater = { onFilter { f -> f.copy(period = f.period?.shifted(1)) } },
        )
    }
    CoShellFilterChip(
        selected = period?.step == PeriodRange.Step.DAYS,
        onClick = { pickerOpen = true },
        label = {
            Text(
                if (period?.step == PeriodRange.Step.DAYS) FilterLabels.period(period, today, locale)
                else stringResource(R.string.filter_period_own),
            )
        },
        leadingIcon = {
            Icon(painterResource(DesignR.drawable.ic_calendar_today), contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
        },
    )
    CoShellHint(stringResource(R.string.filter_period_note))
    if (pickerOpen) {
        RangePickerDialog(
            initial = period,
            onDismiss = { pickerOpen = false },
            onPicked = { first, last ->
                pickerOpen = false
                onFilter { it.copy(period = PeriodRange.days(first, last)) }
            },
        )
    }
}

/** «Октябрь 2026», «Сен – ноя 2026», «2026»: the stepper always says the year. */
private fun stepperLabel(period: PeriodRange, today: LocalDate, locale: java.util.Locale): String {
    val label = FilterLabels.period(period, today, locale)
    val hasYear = period.step == PeriodRange.Step.YEAR || period.start.year != today.year || period.lastDay.year != today.year
    return if (hasYear) label else "$label ${period.start.year}"
}

@Composable
private fun Stepper(label: String, onEarlier: () -> Unit, onLater: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(Sizes.rowDenseMinHeight).background(MaterialTheme.colorScheme.surfaceContainer, PillShape).padding(horizontal = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoShellIconButton(onClick = onEarlier) {
            Icon(painterResource(DesignR.drawable.ic_chevron_right), stringResource(R.string.filter_period_earlier), Modifier.rotate(180f))
        }
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        CoShellIconButton(onClick = onLater) {
            Icon(painterResource(DesignR.drawable.ic_chevron_right), stringResource(R.string.filter_period_later))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangePickerDialog(initial: PeriodRange?, onDismiss: () -> Unit, onPicked: (LocalDate, LocalDate) -> Unit) {
    // The picker counts days in UTC milliseconds.
    fun LocalDate.millis() = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    fun Long.date() = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initial?.start?.millis(),
        initialSelectedEndDateMillis = initial?.lastDay?.millis(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            CoShellTextButton(
                onClick = {
                    val start = state.selectedStartDateMillis
                    if (start == null) onDismiss() else onPicked(start.date(), (state.selectedEndDateMillis ?: start).date())
                },
            ) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = { CoShellTextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    ) {
        DateRangePicker(state = state, modifier = Modifier.weight(1f))
    }
}

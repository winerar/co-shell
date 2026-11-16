package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.theme.selectionColors
import app.coshell.core.designsystem.tokens.Sizes

/** Chosen day of a [CoShellDatePicker], in UTC milliseconds; null while nothing is chosen. */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class DatePick internal constructor(internal val state: DatePickerState) {
    val millis: Long? get() = state.selectedDateMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberDatePick(initialMillis: Long?): DatePick {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    return remember(state) { DatePick(state) }
}

/** Chosen days of a [CoShellDateRangePicker], in UTC milliseconds. */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class DateRangePick internal constructor(internal val state: DateRangePickerState) {
    val startMillis: Long? get() = state.selectedStartDateMillis
    val endMillis: Long? get() = state.selectedEndDateMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberDateRangePick(initialStartMillis: Long?, initialEndMillis: Long?): DateRangePick {
    val state = rememberDateRangePickerState(initialSelectedStartDateMillis = initialStartMillis, initialSelectedEndDateMillis = initialEndMillis)
    return remember(state) { DateRangePick(state) }
}

/**
 * Calendar of one month (board `CompNew`, «Выбор даты», #367 А): round days, the chosen one in the selection colours of
 * the theme, today and the current year in the text accent. Lives inside a sheet; the sheet's «Готово» reads [pick].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoShellDatePicker(pick: DatePick, modifier: Modifier = Modifier) {
    DatePicker(state = pick.state, modifier = modifier, title = null, headline = null, showModeToggle = false, colors = pickerColors())
}

/**
 * Calendar of days, month after month (board `CompNew`, «Выбор периода»): the period is a band of the selection
 * colours. A lazy list, so it has its own bounded height inside the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoShellDateRangePicker(pick: DateRangePick, modifier: Modifier = Modifier) {
    DateRangePicker(
        state = pick.state,
        modifier = modifier.height(Sizes.rangePicker),
        title = null,
        showModeToggle = false,
        colors = pickerColors(),
    )
}

@Composable
private fun pickerColors(): DatePickerColors {
    val ink = accentInk()
    val chosen = selectionColors()
    return DatePickerDefaults.colors(
        todayContentColor = ink,
        todayDateBorderColor = ink,
        currentYearContentColor = ink,
        selectedDayContainerColor = chosen.fill,
        selectedDayContentColor = chosen.content,
    )
}

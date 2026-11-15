package app.coshell.ui.form

import androidx.compose.foundation.layout.height
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.ui.sheet.ModalSheet
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/**
 * «Выберите дни»: a calendar of days in a sheet; «Готово» gives the first and the last day (one tap — one day).
 * Own periods of the operation filters (#141) and of reports (#140).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeSheet(first: LocalDate?, last: LocalDate?, onDismiss: () -> Unit, onPicked: (LocalDate, LocalDate) -> Unit) {
    // The picker counts days in UTC milliseconds.
    fun LocalDate.millis() = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    fun Long.date() = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = first?.millis(),
        initialSelectedEndDateMillis = last?.millis(),
    )
    ModalSheet(
        title = stringResource(R.string.date_range_pick_title),
        actionLabel = stringResource(R.string.keypad_done),
        onAction = { close ->
            val start = state.selectedStartDateMillis
            if (start == null) close() else onPicked(start.date(), (state.selectedEndDateMillis ?: start).date())
        },
        onClosed = onDismiss,
    ) {
        // A lazy month list: it needs a bounded height inside the sheet's scrolling column.
        DateRangePicker(state = state, title = null, showModeToggle = false, modifier = Modifier.height(Sizes.rangePicker))
    }
}

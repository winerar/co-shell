package app.coshell.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellTextButton
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.toJavaLocalDate
import app.coshell.core.designsystem.R as DesignR

/**
 * "Сегодня" / "Вчера" / calendar chip (transaction editor, manual rate dialog). The calendar chip shows the chosen date when it is
 * neither today nor yesterday and opens Material's date picker.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DateChips(
    date: LocalDate,
    today: LocalDate,
    dateMillisUtc: Long,
    onSelect: (LocalDate) -> Unit,
    onPicked: (millisUtc: Long) -> Unit,
) {
    var pickerOpen by rememberSaveable { mutableStateOf(false) }
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    val isOther = date != today && date != yesterday

    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellFilterChip(
            selected = date == today,
            onClick = { onSelect(today) },
            label = { Text(stringResource(R.string.transaction_date_today)) },
        )
        CoShellFilterChip(
            selected = date == yesterday,
            onClick = { onSelect(yesterday) },
            label = { Text(stringResource(R.string.transaction_date_yesterday)) },
        )
        CoShellFilterChip(
            selected = isOther,
            onClick = { pickerOpen = true },
            label = { Text(if (isOther) date.formatLong() else stringResource(R.string.transaction_date_other)) },
            leadingIcon = {
                Icon(
                    painterResource(DesignR.drawable.ic_calendar_today),
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
        )
    }

    if (pickerOpen) {
        DatePickerSheet(
            initialMillisUtc = dateMillisUtc,
            onDismiss = { pickerOpen = false },
            onConfirm = {
                pickerOpen = false
                onPicked(it)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerSheet(initialMillisUtc: Long, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillisUtc)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            CoShellTextButton(onClick = { pickerState.selectedDateMillis?.let(onConfirm) ?: onDismiss() }) {
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            CoShellTextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

/** "8 нояб. 2026 г." in the phone's locale. */
private fun LocalDate.formatLong(): String =
    toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

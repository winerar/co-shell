package app.coshell.ui.form

import androidx.compose.foundation.layout.Arrangement
import app.coshell.core.designsystem.tokens.Sizes
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import app.coshell.ui.sheet.ModalSheet
import app.coshell.core.designsystem.components.CoShellDatePicker
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.rememberDatePick
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
                    modifier = Modifier.size(Sizes.chipIcon),
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

@Composable
private fun DatePickerSheet(initialMillisUtc: Long, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    val pick = rememberDatePick(initialMillisUtc)
    ModalSheet(
        title = stringResource(R.string.date_pick_title),
        actionLabel = stringResource(R.string.keypad_done),
        onAction = { close -> pick.millis?.let(onConfirm) ?: close() },
        onClosed = onDismiss,
    ) {
        CoShellDatePicker(pick)
    }
}

/** "8 нояб. 2026 г." in the phone's locale. */
private fun LocalDate.formatLong(): String =
    toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

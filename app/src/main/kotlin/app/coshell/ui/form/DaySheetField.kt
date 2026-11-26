package app.coshell.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellDayGrid
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.ui.sheet.ModalSheet

/**
 * A day of the month 1–31 in an editor (the card's payment and statement days): the field only shows it, a tap opens the day grid
 * (`CoShellDayGrid`, as «Начало месяца» and «Повтор») — no keyboard at all. [clearable] — an optional day gets «Убрать» in the sheet.
 * [value] / [onValueChange] are the text the view model already takes («15», «» for none).
 */
@Composable
internal fun DaySheetField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    clearable: Boolean = false,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    CoShellTextField(value = value, onValueChange = {}, label = label, modifier = modifier, hint = hint, onClick = { open = true })
    if (open) {
        var day by rememberSaveable { mutableIntStateOf(value.toIntOrNull()?.takeIf { it in 1..MAX_DAY } ?: 0) }
        ModalSheet(
            title = label,
            actionLabel = stringResource(R.string.keypad_done),
            actionEnabled = day != 0,
            onAction = { close ->
                onValueChange(day.toString())
                close()
            },
            onClosed = { open = false },
        ) { close ->
            CoShellDayGrid(selected = day, onSelect = { day = it })
            if (clearable && value.isNotBlank()) {
                CoShellSectionLink(stringResource(R.string.action_clear_value), onClick = {
                    onValueChange("")
                    close()
                })
            }
        }
    }
}

private const val MAX_DAY = 31

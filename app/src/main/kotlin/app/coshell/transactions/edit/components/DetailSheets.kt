package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.ui.form.DateChips
import app.coshell.ui.sheet.FormSheet

/** «Дата» tile → «Сегодня · Вчера · Другая дата»; a quick chip picks and closes, the calendar too. */
@Composable
internal fun DateSheet(state: TransactionEditUiState, actions: TransactionEditActions, onClosed: () -> Unit) {
    FormSheet(stringResource(R.string.transaction_date), onClosed) { close ->
        DateChips(
            state.date,
            state.today,
            state.dateMillisUtc,
            onSelect = {
                actions.onDateSelect(it)
                close()
            },
            onPicked = {
                actions.onDatePicked(it)
                close()
            },
        )
    }
}

/** «Заметка» tile → a text field with the system keyboard (text only — amounts have their own keypad, #291). */
@Composable
internal fun NoteSheet(note: String, onChange: (String) -> Unit, onClosed: () -> Unit) {
    val focus = remember { FocusRequester() }
    FormSheet(stringResource(R.string.transaction_note), onClosed) {
        CoShellTextField(
            value = note,
            onValueChange = onChange,
            label = stringResource(R.string.transaction_note),
            singleLine = false,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth().focusRequester(focus),
        )
    }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
}

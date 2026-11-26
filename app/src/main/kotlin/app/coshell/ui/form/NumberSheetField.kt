package app.coshell.ui.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.keypad.CoShellKeypad
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.designsystem.theme.AmountInputStyle
import app.coshell.ui.sheet.ModalSheet

/**
 * A number of an editor that is not an amount — a percent, days (#300 for the rest of the numbers): the field only shows it,
 * a tap raises [NumberSheet] with our keypad, as [AmountSheetField] does for amounts. Limits — [NumberKeys].
 */
@Composable
internal fun NumberSheetField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    maxWhole: Int,
    modifier: Modifier = Modifier,
    fractionDigits: Int = 0,
    suffix: String? = null,
    hint: String? = null,
    error: String? = null,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    CoShellTextField(
        value = value,
        onValueChange = {},
        label = label,
        modifier = modifier,
        hint = hint,
        error = error,
        suffix = suffix,
        onClick = { open = true },
    )
    if (open) {
        NumberSheet(
            title = label,
            initial = value,
            maxWhole = maxWhole,
            fractionDigits = fractionDigits,
            suffix = suffix,
            onDone = onValueChange,
            onClosed = { open = false },
        )
    }
}

/**
 * The sheet: the number in large figures above the keypad (no «+»/«−»); «Готово» hands it to [onDone] and closes, the × or a swipe
 * closes without it. [accept] — whether «Готово» is allowed for the typed text (the card's digits: none or all four); [footer] —
 * hints under the keypad's number.
 */
@Composable
internal fun NumberSheet(
    title: String,
    initial: String,
    maxWhole: Int,
    onDone: (String) -> Unit,
    onClosed: () -> Unit,
    fractionDigits: Int = 0,
    suffix: String? = null,
    placeholder: String = "0",
    accept: (String) -> Boolean = { true },
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    var text by rememberSaveable { mutableStateOf(NumberKeys.normalize(initial, maxWhole, fractionDigits)) }
    var done by remember { mutableStateOf(false) }
    ModalSheet(title = title, onClosed = { if (done) onDone(text); onClosed() }) { close ->
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            Text(
                text.ifEmpty { placeholder } + suffix?.let { " $it" }.orEmpty(),
                style = AmountInputStyle,
                textAlign = TextAlign.End,
                color = if (text.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
        }
        footer()
        CoShellKeypad(
            onKey = { key ->
                if (key == KeypadKey.Done) {
                    done = true
                    close()
                } else {
                    text = NumberKeys.apply(text, key, maxWhole, fractionDigits)
                }
            },
            doneLabel = stringResource(R.string.keypad_done),
            backspaceLabel = stringResource(R.string.keypad_backspace),
            doneEnabled = accept(text),
            showDecimal = fractionDigits > 0,
            showPlus = false,
        )
    }
}

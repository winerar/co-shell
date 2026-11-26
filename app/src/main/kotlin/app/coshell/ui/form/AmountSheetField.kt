package app.coshell.ui.form

import androidx.compose.foundation.layout.Column
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
import app.coshell.core.designsystem.components.CoShellAmountField
import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.designsystem.components.keypad.CoShellKeypad
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.designsystem.theme.AmountInputStyle
import app.coshell.core.model.Currencies
import app.coshell.core.model.CurrencyCode
import app.coshell.ui.sheet.ModalSheet

/**
 * An amount of an editor (#300, owner poll 2026-11-24): the field only shows the number; a tap raises [AmountSheet] with our own
 * keypad — never the system one. [value] / [onValueChange] are plain text as the screen's view model already parses it
 * (`1234,5`); the field shows it grouped. [currency] sets the fraction digits (none — two) and the grey code after the number.
 */
@Composable
internal fun AmountSheetField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    currency: String? = null,
    error: String? = null,
    hint: String? = null,
    sheetTitle: String = label,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    CoShellAmountField(
        value = if (value.isBlank()) "" else AmountKeys.display(AmountKeys.normalize(value)),
        onValueChange = {},
        label = label,
        modifier = modifier,
        currency = currency,
        error = error,
        hint = hint,
        onClick = { open = true },
    )
    if (open) {
        AmountSheet(
            title = sheetTitle,
            initial = value,
            currency = currency,
            onDone = { onValueChange(it) },
            onClosed = { open = false },
        )
    }
}

/**
 * The sheet itself: the number in large figures above the keypad; «Готово» hands the typed number to [onDone] and closes,
 * the × or a swipe closes without it ([onClosed] runs in both cases, after the sheet slid down — as `ChoiceSheet`).
 */
@Composable
internal fun AmountSheet(title: String, initial: String, currency: String?, onDone: (String) -> Unit, onClosed: () -> Unit) {
    val digits = fractionDigitsOf(currency)
    var text by rememberSaveable { mutableStateOf(AmountKeys.normalize(initial)) }
    var done by remember { mutableStateOf(false) }
    ModalSheet(title = title, onClosed = { if (done) onDone(text); onClosed() }) { close ->
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            Text(
                AmountKeys.display(text) + currency?.let { " $it" }.orEmpty(),
                style = AmountInputStyle,
                textAlign = TextAlign.End,
                color = if (text.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
        }
        CoShellKeypad(
            onKey = { key ->
                if (key == KeypadKey.Done) {
                    done = true
                    close()
                } else {
                    text = AmountKeys.apply(text, key, digits)
                }
            },
            doneLabel = stringResource(R.string.keypad_done),
            backspaceLabel = stringResource(R.string.keypad_backspace),
            showDecimal = digits > 0,
            showPlus = false,
        )
    }
}

/** Fraction digits of the currency named by [code]; two when there is none or it is not a code. */
internal fun fractionDigitsOf(code: String?): Int =
    code?.let { CurrencyCode.parseOrNull(it) }?.let { Currencies.info(it).fractionDigits } ?: 2

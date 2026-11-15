package app.coshell.rates.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellSheet
import app.coshell.core.designsystem.components.CoShellSheetDialogProperties
import app.coshell.core.designsystem.components.CoShellSheetLayout
import app.coshell.core.designsystem.components.keypad.CoShellKeypad
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.designsystem.theme.AmountInputStyle
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.Currencies
import app.coshell.rates.list.ConverterUi

/** The amount of the converter, typed on the own keypad (owner: sums — only the own keypad, #291); «Готово» closes. */
@Composable
internal fun ConverterKeypadSheet(converter: ConverterUi, onKey: (KeypadKey) -> Unit, onClosed: () -> Unit) {
    Dialog(onDismissRequest = onClosed, properties = CoShellSheetDialogProperties) {
        CoShellSheet(onClosed = onClosed) { close ->
            CoShellSheetLayout(title = stringResource(R.string.rates_converter_title), actionLabel = "", onAction = close, showAction = false) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.m),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(converter.amountText, style = AmountInputStyle, maxLines = 1)
                    Text(converter.from, style = RateTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                CoShellKeypad(
                    onKey = { key -> if (key == KeypadKey.Done) close() else onKey(key) },
                    doneLabel = stringResource(R.string.keypad_done),
                    backspaceLabel = stringResource(R.string.keypad_backspace),
                    showDecimal = Currencies.info(converter.from).fractionDigits > 0,
                    modifier = Modifier.padding(horizontal = Spacing.m).padding(bottom = Spacing.m),
                )
            }
        }
    }
}

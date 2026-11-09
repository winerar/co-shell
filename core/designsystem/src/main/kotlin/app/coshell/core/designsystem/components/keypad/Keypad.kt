package app.coshell.core.designsystem.components.keypad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Own amount keypad of the entry sheet (owner 2026-11-09: «удобно одним пальцем»): digits, `000`, decimal,
 * `+` for adding up a receipt, backspace and a tall «Готово» under the thumb (bottom right). Fixed height —
 * the sheet does not jump as with the system keyboard. Every key sinks and springs back (tactile system)
 * with a keyboard tick.
 */
@Composable
fun CoShellKeypad(
    onKey: (KeypadKey) -> Unit,
    doneLabel: String,
    backspaceLabel: String,
    modifier: Modifier = Modifier,
    doneEnabled: Boolean = true,
    showDecimal: Boolean = true,
) {
    val haptics = LocalHapticFeedback.current
    val press = { key: KeypadKey ->
        haptics.performHapticFeedback(if (key == KeypadKey.Done) HapticFeedbackType.Confirm else HapticFeedbackType.KeyboardTap)
        onKey(key)
    }
    val rows = Sizes.keypadKey * 4 + Spacing.s * 3
    Row(modifier.fillMaxWidth().height(rows), horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        listOf("789", "456", "123").let { lines ->
            for (col in 0..2) {
                KeyColumn {
                    lines.forEach { line -> DigitKey(line[col]) { press(KeypadKey.Digit(line[col])) } }
                    when (col) {
                        0 -> if (showDecimal) TextKey(",") { press(KeypadKey.Decimal) } else Spacer()
                        1 -> DigitKey('0') { press(KeypadKey.Digit('0')) }
                        else -> TextKey("000") { press(KeypadKey.TripleZero) }
                    }
                }
            }
        }
        KeyColumn {
            Key(onClick = { press(KeypadKey.Backspace) }, container = MaterialTheme.colorScheme.surfaceContainerHighest) {
                Icon(painterResource(R.drawable.ic_backspace), contentDescription = backspaceLabel)
            }
            Key(onClick = { press(KeypadKey.Plus) }, container = MaterialTheme.colorScheme.surfaceContainerHighest) {
                Text("+", style = AmountTitleStyle)
            }
            Key(
                onClick = { if (doneEnabled) press(KeypadKey.Done) },
                container = MaterialTheme.colorScheme.primary,
                content = MaterialTheme.colorScheme.onPrimary,
                weight = 2f,
                tactile = Tactile.Primary,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painterResource(R.drawable.ic_check), contentDescription = null)
                    Text(doneLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.KeyColumn(content: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.weight(1f).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(Spacing.s), content = content)

@Composable
private fun ColumnScope.DigitKey(digit: Char, onClick: () -> Unit) = TextKey(digit.toString(), onClick)

@Composable
private fun ColumnScope.TextKey(text: String, onClick: () -> Unit) = Key(onClick = onClick) {
    Text(text, style = AmountTitleStyle)
}

@Composable
private fun ColumnScope.Spacer() = Box(Modifier.weight(1f))

@Composable
private fun ColumnScope.Key(
    onClick: () -> Unit,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: Color = MaterialTheme.colorScheme.onSurface,
    weight: Float = 1f,
    tactile: Tactile = Tactile.Control,
    label: @Composable () -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxWidth()
            .tactileClickable(tactile, shape, onClick = onClick)
            .background(container, shape),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides content,
            content = label,
        )
    }
}

@Preview
@Composable
private fun KeypadPreview() = CoShellTheme(darkTheme = true) {
    CoShellKeypad(onKey = {}, doneLabel = "Готово", backspaceLabel = "Стереть")
}

@Preview
@Composable
private fun KeypadLightPreview() = CoShellTheme(darkTheme = false) {
    CoShellKeypad(onKey = {}, doneLabel = "Готово", backspaceLabel = "Стереть")
}

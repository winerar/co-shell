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
import androidx.compose.ui.unit.sp
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.components.fillEdge
import app.coshell.core.designsystem.amount.fitAmountAutoSize
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Own amount keypad of the entry sheet (owner 2026-11-09: «удобно одним пальцем»): digits, `000`, decimal,
 * `+` and `−` for adding up a receipt (and taking a discount off), backspace and «Готово» under the thumb (bottom right). Fixed height —
 * the sheet does not jump as with the system keyboard. Every key sinks and springs back (tactile system)
 * with a keyboard tick. [showPlus] = false — a single number (a rate), no adding up.
 */
@Composable
fun CoShellKeypad(
    onKey: (KeypadKey) -> Unit,
    doneLabel: String,
    backspaceLabel: String,
    modifier: Modifier = Modifier,
    doneEnabled: Boolean = true,
    showDecimal: Boolean = true,
    showPlus: Boolean = true,
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
            if (showPlus) {
                Key(onClick = { press(KeypadKey.Plus) }, container = MaterialTheme.colorScheme.surfaceContainerHighest) {
                    Text("+", style = AmountTitleStyle)
                }
                Key(onClick = { press(KeypadKey.Minus) }, container = MaterialTheme.colorScheme.surfaceContainerHighest) {
                    Text("−", style = AmountTitleStyle)
                }
            } else {
                Spacer()
            }
            Key(
                onClick = { if (doneEnabled) press(KeypadKey.Done) },
                // Quiet until there is something to post (board of #505): the same key at 38 %.
                container = MaterialTheme.colorScheme.primary.copy(alpha = if (doneEnabled) 1f else DONE_OFF_ALPHA),
                content = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (doneEnabled) 1f else DONE_OFF_ALPHA),
                // One row next to «+ −» (owner 2026-11-18, #291); tall when the column has no operators.
                weight = if (showPlus) 1f else 2f,
                tactile = Tactile.Primary,
            ) {
                DoneLabel(doneLabel, inRow = showPlus)
            }
        }
    }
}

private const val DONE_OFF_ALPHA = 0.38f

@Composable
private fun DoneLabel(label: String, inRow: Boolean) {
    val content: @Composable () -> Unit = {
        Icon(painterResource(R.drawable.ic_check), contentDescription = null)
        // «Провести» on a narrow screen with a big font shrinks to fit, never breaks into «Прове / сти».
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            autoSize = fitAmountAutoSize(min = 8.sp, max = MaterialTheme.typography.labelLarge.fontSize),
        )
    }
    if (inRow) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs), verticalAlignment = Alignment.CenterVertically) { content() }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { content() }
    }
}

@Composable
internal fun androidx.compose.foundation.layout.RowScope.KeyColumn(content: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.weight(1f).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(Spacing.s), content = content)

@Composable
private fun ColumnScope.DigitKey(digit: Char, onClick: () -> Unit) = TextKey(digit.toString(), onClick)

@Composable
private fun ColumnScope.TextKey(text: String, onClick: () -> Unit) = Key(onClick = onClick) {
    Text(text, style = AmountTitleStyle)
}

@Composable
internal fun ColumnScope.Spacer() = Box(Modifier.weight(1f))

@Composable
internal fun ColumnScope.Key(
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
            .background(container, shape)
            .fillEdge(container, shape),
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

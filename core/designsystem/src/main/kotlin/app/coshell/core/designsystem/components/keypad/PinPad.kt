package app.coshell.core.designsystem.components.keypad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.components.drawFillEdge
import app.coshell.core.designsystem.components.fillEdge
import androidx.compose.ui.geometry.Size
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke

/** A key of [CoShellPinPad]. */
sealed interface PinKey {
    data class Digit(val digit: Char) : PinKey
    data object Backspace : PinKey
    data object Biometric : PinKey
}

/**
 * The PIN pad of the lock screen (#145, board `Priv145LockA`: the amount keypad's cells, owner's poll 2026-11-20 «А»):
 * 1–9, then fingerprint · 0 · erase. [biometricLabel] = `null` — no fingerprint key (creating a PIN, phone without it).
 * Same keys, sizes and tactile tick as [CoShellKeypad], in the theme's colours.
 */
@Composable
fun CoShellPinPad(
    onKey: (PinKey) -> Unit,
    backspaceLabel: String,
    modifier: Modifier = Modifier,
    biometricLabel: String? = null,
    enabled: Boolean = true,
) {
    val haptics = LocalHapticFeedback.current
    val press = { key: PinKey ->
        if (enabled) {
            haptics.performHapticFeedback(HapticFeedbackType.KeyboardTap)
            onKey(key)
        }
    }
    val height = Sizes.keypadKey * 4 + Spacing.s * 3
    Row(modifier.fillMaxWidth().height(height), horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        for (col in 0..2) {
            KeyColumn {
                listOf("123", "456", "789").forEach { line ->
                    val d = line[col]
                    Key(onClick = { press(PinKey.Digit(d)) }) { Text(d.toString(), style = AmountTitleStyle) }
                }
                when (col) {
                    0 -> if (biometricLabel != null) {
                        Key(onClick = { press(PinKey.Biometric) }, container = MaterialTheme.colorScheme.surfaceContainerHighest) {
                            Icon(painterResource(R.drawable.ic_fingerprint), contentDescription = biometricLabel)
                        }
                    } else {
                        Spacer()
                    }
                    1 -> Key(onClick = { press(PinKey.Digit('0')) }) { Text("0", style = AmountTitleStyle) }
                    else -> Key(onClick = { press(PinKey.Backspace) }, container = MaterialTheme.colorScheme.surfaceContainerHighest) {
                        Icon(painterResource(R.drawable.ic_backspace), contentDescription = backspaceLabel)
                    }
                }
            }
        }
    }
}

/**
 * How many PIN digits are typed (#145): [length] rings, the first [filled] — the theme's neon with a fine edge where needed;
 * [error] — the theme's error colour (PIN did not match). [description] — for TalkBack («Введено 2 из 4»).
 */
@Composable
fun CoShellPinDots(filled: Int, description: String, modifier: Modifier = Modifier, length: Int = 4, error: Boolean = false) {
    val fill = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val ring = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
    val edge = fillEdge(fill, MaterialTheme.colorScheme.background)
    Row(modifier.semantics { contentDescription = description }, horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
        repeat(length) { i ->
            val on = i < filled
            Canvas(Modifier.size(Sizes.pinDot)) {
                val r = size.minDimension / 2
                if (on) {
                    drawCircle(fill, r)
                    drawFillEdge(edge, Offset.Zero, Size(size.width, size.height), r)
                } else {
                    val w = Sizes.pinDotStroke.toPx()
                    drawCircle(ring, r - w / 2, Offset(r, r), style = Stroke(w))
                }
            }
        }
    }
}

@Preview
@Composable
private fun PinPadPreview() = CoShellTheme(darkTheme = true) {
    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        Box { CoShellPinDots(filled = 2, description = "2 из 4") }
        CoShellPinPad(onKey = {}, backspaceLabel = "Стереть", biometricLabel = "Отпечаток")
    }
}

@Preview
@Composable
private fun PinPadLightPreview() = CoShellTheme(darkTheme = false) {
    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        CoShellPinDots(filled = 4, description = "4 из 4", error = true)
        CoShellPinPad(onKey = {}, backspaceLabel = "Стереть")
    }
}

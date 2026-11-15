package app.coshell.core.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.tokens.Radii
import app.coshell.core.designsystem.tokens.Spacing

/** One line of a [CoShellPeekBubble]: «≈ 31,53 USD», or [amount] = null and [missing] — «нет курса». */
data class PeekLine(val amount: String?, val currency: String, val missing: String = "")

/**
 * Bubble over the element it is shown from (#318, board `Fb318A`): amounts in other currencies, one per line,
 * and [footnote] in small grey under them («по курсу на 12 ноября»). Lives while the finger holds the amount
 * (`Modifier.holdToPeek`), so it takes no touches; above the element when there is room, otherwise below,
 * its right edge at the element's right edge and kept inside the window.
 */
@Composable
fun CoShellPeekBubble(lines: List<PeekLine>, footnote: String) {
    val density = LocalDensity.current
    val position = remember(density) { PeekPosition(density) }
    Popup(popupPositionProvider = position, properties = PopupProperties(focusable = false, clippingEnabled = true)) {
        PeekCard(lines, footnote)
    }
}

/** The bubble itself, without the popup — for previews and screenshots. */
@Composable
fun PeekCard(lines: List<PeekLine>, footnote: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainerHigh,
        shadowElevation = 12.dp,
        modifier = modifier
            .widthIn(min = 180.dp, max = 280.dp)
            .border(1.dp, scheme.outlineVariant, MaterialTheme.shapes.large),
    ) {
        Column(Modifier.padding(horizontal = Spacing.m + Spacing.xxs, vertical = Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            lines.forEach { PeekRow(it) }
            Text(
                footnote,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
    }
}

@Composable
private fun PeekRow(line: PeekLine) {
    val scheme = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
        if (line.amount != null) {
            Text("≈ ${line.amount}", style = AmountTextStyle, color = scheme.onSurface, modifier = Modifier.weight(1f))
        } else {
            Text(line.missing, style = MaterialTheme.typography.bodyMedium, color = LocalStatusPalette.current.warning, modifier = Modifier.weight(1f))
        }
        Text(line.currency, style = AmountTextStyle, color = scheme.onSurfaceVariant)
    }
}

/** Above the anchor (a gap of [Radii.small]), right edges aligned; below it when the top has no room. */
private class PeekPosition(density: Density) : PopupPositionProvider {
    private val gap = with(density) { Radii.small.roundToPx() }
    private val edge = with(density) { Spacing.l.roundToPx() }

    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
        val x = (anchorBounds.right - popupContentSize.width).coerceIn(edge, (windowSize.width - popupContentSize.width - edge).coerceAtLeast(edge))
        val above = anchorBounds.top - gap - popupContentSize.height
        val y = if (above >= edge) above else anchorBounds.bottom + gap
        return IntOffset(x, y)
    }
}

package app.coshell.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * A step of a vertical timeline (board `CompNew`, «Точка и статус», #367 А): the dot — neon once [reached] — and, below
 * it, the line to the next step ([lineReached] `null` — the last step, no line). Give it the height of the step's text.
 */
@Composable
fun CoShellTimelineDot(reached: Boolean, lineReached: Boolean?, modifier: Modifier = Modifier) {
    val on = MaterialTheme.colorScheme.primary
    val off = MaterialTheme.colorScheme.outlineVariant
    Box(modifier.width(Sizes.timelineDot), contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.width(Sizes.timelineDot).fillMaxHeight()) {
            val r = size.width / 2
            val cy = r + Spacing.xs.toPx()
            if (lineReached != null) {
                drawLine(if (lineReached) on else off, Offset(r, cy), Offset(r, size.height), strokeWidth = Sizes.selectionRing.toPx())
            }
            drawCircle(if (reached) on else off, radius = r, center = Offset(r, cy))
        }
    }
}

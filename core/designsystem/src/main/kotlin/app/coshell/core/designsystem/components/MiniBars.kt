package app.coshell.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes

/**
 * Tiny bar chart in a tile (expenses per day of the month): one rounded bar per value, the tallest fills
 * [Sizes.miniBarsHeight]. Bars from index [elapsed] on (days still ahead) are faint dots; [highlight] (today)
 * is drawn in full [color], the others a bit softer. The bars grow from the baseline the first time they show.
 * Values are plain magnitudes (minor units); only their ratio matters — see [miniBarFractions].
 */
@Composable
fun CoShellMiniBars(
    values: List<Long>,
    color: Color,
    modifier: Modifier = Modifier,
    elapsed: Int = values.size,
    highlight: Int? = null,
) {
    val fractions = remember(values) { miniBarFractions(values) }
    val shown = rememberSaveable { booleanArrayOf(false) }
    val grow = remember { Animatable(if (shown[0]) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (shown[0]) return@LaunchedEffect
        shown[0] = true
        grow.animateTo(1f, Motion.spatialSlow())
    }
    Canvas(modifier.fillMaxWidth().height(Sizes.miniBarsHeight)) {
        if (fractions.isEmpty()) return@Canvas
        val slot = size.width / fractions.size
        val barWidth = slot * BAR_SHARE
        val minHeight = Sizes.miniBarMin.toPx()
        val radius = CornerRadius(barWidth / 2, barWidth / 2)
        fractions.forEachIndexed { index, fraction ->
            val ahead = index >= elapsed
            val target = if (ahead || fraction == 0f) minHeight else maxOf(minHeight, fraction * size.height)
            val height = if (ahead) minHeight else minHeight + (target - minHeight) * grow.value
            val alpha = when {
                ahead -> AHEAD_ALPHA
                index == highlight -> 1f
                fraction == 0f -> AHEAD_ALPHA
                else -> PAST_ALPHA
            }
            drawRoundRect(
                color = color.copy(alpha = color.alpha * alpha),
                topLeft = Offset(index * slot + (slot - barWidth) / 2, size.height - height),
                size = Size(barWidth, height),
                cornerRadius = radius,
            )
        }
    }
}

/** Each value as a share of the largest (0..1); all zeros (or negatives) give zeros. */
fun miniBarFractions(values: List<Long>): List<Float> {
    val max = values.maxOrNull()?.takeIf { it > 0 } ?: return values.map { 0f }
    return values.map { (it.coerceAtLeast(0).toDouble() / max).toFloat() }
}

private const val BAR_SHARE = 0.6f
private const val PAST_ALPHA = 0.6f
private const val AHEAD_ALPHA = 0.25f

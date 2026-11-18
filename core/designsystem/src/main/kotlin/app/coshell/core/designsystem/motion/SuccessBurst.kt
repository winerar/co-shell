package app.coshell.core.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import app.coshell.core.designsystem.components.fillEdge
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import kotlin.math.cos
import kotlin.math.sin

/**
 * The moment of «готово» (update done, poll 2026-11-18, «ореол и искры»): once, a halo ring in the second colour widens round [content]
 * and eight sparks of the two theme colours fly out and fade; one `Confirm` tick; then everything is still. A spring, so no
 * durations; nothing loops. Sparks of the neon get the same thin edge as every neon on white (`fillEdge`).
 */
@Composable
fun CoShellSuccessBurst(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val progress = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        progress.animateTo(1f, Motion.spatialSlow())
    }
    val colors = MaterialTheme.colorScheme
    val edge = fillEdge(colors.primary, colors.surface)?.brush
    Box(
        modifier
            .padding(Spacing.l)
            .drawBehind {
                val p = progress.value.coerceIn(0f, 1.2f)
                val fade = (1f - p).coerceIn(0f, 1f)
                val reach = size.minDimension / 2
                val hair = Sizes.hairline.toPx()
                drawCircle(colors.secondary.copy(alpha = fade * HALO_ALPHA), reach * (1f + HALO_GROWTH * p), center, style = Stroke(hair * 2))
                for (i in 0 until SPARKS) {
                    val angle = i * (2 * Math.PI / SPARKS)
                    val at = center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * (reach * (SPARK_FROM + SPARK_TRAVEL * p))
                    val spark = if (i % 2 == 0) colors.primary else colors.secondary
                    val r = reach * SPARK_SIZE
                    drawCircle(spark.copy(alpha = fade), r, at)
                    if (i % 2 == 0) edge?.let { drawCircle(it, r, at, alpha = fade, style = Stroke(hair)) }
                }
            },
        content = content,
    )
}

private const val SPARKS = 8
private const val HALO_ALPHA = 0.5f
private const val HALO_GROWTH = 0.3f
private const val SPARK_FROM = 1.05f
private const val SPARK_TRAVEL = 0.4f
private const val SPARK_SIZE = 0.04f

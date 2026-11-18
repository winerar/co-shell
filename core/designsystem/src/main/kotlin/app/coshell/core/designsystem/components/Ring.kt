package app.coshell.core.designsystem.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.theme.progressColors
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes

/**
 * The round progress of the app («лайм с обводкой», poll 2026-11-18): a quiet track and an arc in the palette's neon, with a
 * thin deep edge on white where the neon would melt into the page (`fillEdge`). [progress] 0…1 fills the arc, `null` —
 * a short arc turns round (work of unknown length). The one ring for the hero ([ProgressHero]) and the spinner ([CoShellLoading]).
 */
@Composable
internal fun CoShellRing(size: Dp, stroke: Dp, progress: Float?, modifier: Modifier = Modifier) {
    val colors = progressColors()
    val edge = fillEdge(colors.indicator, colors.track)?.brush
    if (progress == null) {
        val turn by rememberInfiniteTransition(label = "ringSpin").animateFloat(0f, FULL_TURN, Motion.spin(), label = "ringTurn")
        Canvas(modifier.size(size)) { drawRing(colors.track, colors.indicator, edge, stroke.toPx(), turn - QUARTER, SPIN_SWEEP) }
    } else {
        Canvas(modifier.size(size)) { drawRing(colors.track, colors.indicator, edge, stroke.toPx(), -QUARTER, FULL_TURN * progress.coerceIn(0f, 1f)) }
    }
}

private fun DrawScope.drawRing(track: Color, arc: Color, edge: Brush?, stroke: Float, start: Float, sweep: Float) {
    val inset = stroke / 2 + EDGE_ROOM.toPx()
    val topLeft = Offset(inset, inset)
    val box = Size(size.width - 2 * inset, size.height - 2 * inset)
    drawArc(track, 0f, FULL_TURN, useCenter = false, topLeft = topLeft, size = box, style = Stroke(stroke))
    if (sweep <= 0f) return
    edge?.let {
        drawArc(it, start, sweep, useCenter = false, topLeft = topLeft, size = box,
            style = Stroke(stroke + 2 * Sizes.hairline.toPx(), cap = StrokeCap.Round))
    }
    drawArc(arc, start, sweep, useCenter = false, topLeft = topLeft, size = box, style = Stroke(stroke, cap = StrokeCap.Round))
}

/**
 * The check mark of «готово»: [color] (the neon by default) with a thin deep edge where it would melt into [over]
 * (the disc it sits on). Drawn by hand, because an icon cannot have an edge.
 */
@Composable
fun CoShellCheckMark(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = progressColors().indicator,
    over: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {
    val edge = fillEdge(color, over)?.brush
    Canvas(modifier.size(size)) {
        val unit = this.size.minDimension / CHECK_GRID
        val path = Path().apply {
            moveTo(5f * unit, 12f * unit)
            lineTo(10f * unit, 17f * unit)
            lineTo(19f * unit, 7f * unit)
        }
        val width = CHECK_WIDTH * unit
        edge?.let { drawPath(path, it, style = Stroke(width + 2 * Sizes.hairline.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)) }
        drawPath(path, color, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private const val FULL_TURN = 360f
private const val QUARTER = 90f
private const val SPIN_SWEEP = 100f
private const val CHECK_GRID = 24f
private const val CHECK_WIDTH = 2.4f
private val EDGE_ROOM = 1.dp

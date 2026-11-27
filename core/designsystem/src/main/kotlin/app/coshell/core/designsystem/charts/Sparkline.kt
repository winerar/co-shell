package app.coshell.core.designsystem.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * A small line without axes (#195, board `Idea10A`: a currency's rate for 30 days in its row): [points] left to right
 * on their own scale (the lowest at the bottom, the highest at the top — the trend, not the numbers; the words next to
 * it say «−6,1 %»). Fewer than two points draw nothing. [description] is for TalkBack.
 */
@Composable
fun CoShellSparkline(points: List<Float>, color: Color, description: String, modifier: Modifier = Modifier) {
    Canvas(modifier.size(WIDTH, HEIGHT).semantics { contentDescription = description }) {
        if (points.size < 2) return@Canvas
        val stroke = STROKE.toPx()
        val low = points.min()
        val span = (points.max() - low).takeIf { it > 0f } ?: 1f
        val inner = size.height - stroke * 2
        val path = Path()
        points.forEachIndexed { i, p ->
            val at = Offset(size.width * i / (points.size - 1), stroke + inner * (1f - (p - low) / span))
            if (i == 0) path.moveTo(at.x, at.y) else path.lineTo(at.x, at.y)
        }
        drawPath(path, color, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private val WIDTH = 72.dp
private val HEIGHT = 28.dp
private val STROKE = 1.8.dp

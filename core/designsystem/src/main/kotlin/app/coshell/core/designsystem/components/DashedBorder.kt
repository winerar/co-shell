package app.coshell.core.designsystem.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * A dashed outline — the look of an empty slot that adds something: «+ Новый шаблон», «Пригласить»,
 * «+ Добавить курс». [cornerRadius] larger than half the height makes a pill (or a circle).
 */
fun Modifier.dashedBorder(color: Color, cornerRadius: Dp): Modifier = drawBehind {
    val width = Sizes.hairline.toPx() * DASH_WIDTH
    val dash = Spacing.xs.toPx()
    val radius = cornerRadius.toPx().coerceAtMost(size.minDimension / 2)
    drawRoundRect(
        color = color,
        topLeft = Offset(width / 2, width / 2),
        size = Size(size.width - width, size.height - width),
        cornerRadius = CornerRadius(radius),
        style = Stroke(width, pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash))),
    )
}

private const val DASH_WIDTH = 1.5f

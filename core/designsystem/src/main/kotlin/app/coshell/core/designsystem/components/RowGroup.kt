package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.motion.smoothResize
import app.coshell.core.designsystem.tokens.Radii

// Rows in one card (К5, review 2026-11-10): service screens (settings, family, backup, rates, «Ещё»)
// keep their rows in a `surfaceContainer` card with 16 dp corners, a thin border, and a 1 dp line between rows.
// Two ways in: [CoShellRowGroup] for a short fixed group, [Modifier.rowGroupSegment] for long lazy lists
// (each item draws its own piece of the card, so the list stays lazy).

/** Where a row sits in its group: decides which corners are round and whether a line goes above it. */
@Immutable
data class RowGroupPosition(val index: Int, val count: Int) {
    val isFirst: Boolean get() = index == 0
    val isLast: Boolean get() = index == count - 1
}

/** Collects the rows of a [CoShellRowGroup]; `if` around [row] is fine — lines follow the rows actually shown. */
class RowGroupScope internal constructor() {
    internal val rows = mutableListOf<@Composable () -> Unit>()

    fun row(content: @Composable () -> Unit) {
        rows += content
    }
}

/**
 * A card of rows: «Общее» in settings, members of the family, the auto-backup block. Put
 * [CoShellListRow]/[CoShellSwitchRow] inside — they are transparent and take the card's colour.
 * Nothing is drawn when no row is added. The card follows its rows' height smoothly.
 */
@Composable
fun CoShellRowGroup(modifier: Modifier = Modifier, rows: RowGroupScope.() -> Unit) {
    val list = RowGroupScope().apply(rows).rows
    if (list.isEmpty()) return
    Column(modifier.fillMaxWidth().smoothResize()) {
        list.forEachIndexed { index, row ->
            Column(Modifier.rowGroupSegment(RowGroupPosition(index, list.size))) { row() }
        }
    }
}

/**
 * Draws this row's piece of a group card: background and side borders, the round top on the first row,
 * the round bottom on the last, a 1 dp line above every row but the first. For `LazyColumn` items:
 * `items(list) { Box(Modifier.padding(horizontal = 16.dp).rowGroupSegment(position)) { … } }`.
 */
@Composable
fun Modifier.rowGroupSegment(position: RowGroupPosition): Modifier {
    val colors = MaterialTheme.colorScheme
    val style = RowGroupStyle(colors.surfaceContainer, colors.outlineVariant, colors.surfaceContainerHighest)
    val top = if (position.isFirst) Radii.large else 0.dp
    val bottom = if (position.isLast) Radii.large else 0.dp
    // Clip to the card's shape so a pressed row's light stays inside the round corners.
    val shape = RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
    return this.clip(shape).drawBehind { drawSegment(position, style) }
}

private data class RowGroupStyle(val fill: Color, val border: Color, val divider: Color)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSegment(position: RowGroupPosition, style: RowGroupStyle) {
    val radius = Radii.large.toPx()
    val stroke = BORDER.toPx()
    // The whole card's outline, stretched past this row on the sides that continue in the next rows;
    // clipping to the row leaves only its own piece (straight sides in the middle, round ends at the edges).
    val top = if (position.isFirst) 0f else -2 * radius
    val bottom = if (position.isLast) size.height else size.height + 2 * radius
    val corner = CornerRadius(radius)
    drawRoundRect(style.fill, topLeft = Offset(0f, top), size = Size(size.width, bottom - top), cornerRadius = corner)
    val half = stroke / 2
    drawRoundRect(
        color = style.border,
        topLeft = Offset(half, top + half),
        size = Size(size.width - stroke, bottom - top - stroke),
        cornerRadius = CornerRadius(radius - half),
        style = Stroke(stroke),
    )
    if (!position.isFirst) {
        drawRect(style.divider, topLeft = Offset(stroke, 0f), size = Size(size.width - 2 * stroke, stroke))
    }
}

private val BORDER = 1.dp

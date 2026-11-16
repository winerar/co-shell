package app.coshell.transactions.list.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import app.coshell.core.designsystem.components.CoShellTagMore
import app.coshell.core.designsystem.components.CoShellTagPill
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.TransactionsUiState.Row as RowState

/**
 * Up to two tag pills and «+N» under the account line (board `Fb129AOps`). «+N» never wraps; the pills keep their own
 * width while they fit and, when they don't, shrink — the widest first — and end in «…» ([fitWidths]).
 */
@Composable
internal fun RowTags(row: RowState) {
    Layout(
        content = {
            row.tags.forEach { CoShellTagPill(it.name, it.color) }
            if (row.moreTags > 0) CoShellTagMore(row.moreTags)
        },
        modifier = Modifier.padding(top = Spacing.xs),
    ) { measurables, constraints ->
        val gap = Spacing.xs.roundToPx()
        val pills = measurables.take(row.tags.size)
        val more = measurables.drop(row.tags.size).firstOrNull()?.measure(Constraints())
        val gaps = gap * (measurables.size - 1).coerceAtLeast(0)
        val forPills = constraints.maxWidth - gaps - (more?.width ?: 0)
        val widths = fitWidths(pills.map { it.maxIntrinsicWidth(constraints.maxHeight) }, forPills)
        val placed = pills.mapIndexed { i, m -> m.measure(Constraints(maxWidth = widths[i])) } + listOfNotNull(more)
        val height = placed.maxOfOrNull { it.height } ?: 0
        val width = (placed.sumOf { it.width } + gaps).coerceAtMost(constraints.maxWidth)
        layout(width, height) {
            var x = 0
            placed.forEach { p ->
                p.placeRelative(x, (height - p.height) / 2)
                x += p.width + gap
            }
        }
    }
}

/**
 * Widths for items of [natural] widths in [available] space: everyone keeps its own width if all fit; otherwise the
 * narrow ones keep theirs and the space left is split evenly among the wider ones («water filling»). Order is kept.
 */
internal fun fitWidths(natural: List<Int>, available: Int): List<Int> {
    val result = IntArray(natural.size)
    var left = available.coerceAtLeast(0)
    var count = natural.size
    natural.indices.sortedBy { natural[it] }.forEach { i ->
        val share = left / count
        result[i] = minOf(natural[i], share)
        left -= result[i]
        count--
    }
    return result.toList()
}

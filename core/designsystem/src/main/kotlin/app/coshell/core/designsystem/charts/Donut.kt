package app.coshell.core.designsystem.charts

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.components.fillEdgeColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.animation.core.animate
import app.coshell.core.designsystem.tokens.Motion
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A slice of [CoShellDonut]: a plain magnitude (minor units of one currency) and the category colour. */
@Immutable
data class DonutSlice(val value: Long, val color: Color)

/** Where one slice is drawn: degrees clockwise from 12 o'clock. */
@Immutable
data class DonutArc(val start: Float, val sweep: Float)

/**
 * A ring of shares (structure of spending): at most [DONUT_MAX_SLICES] categories plus «Другое» — the caller groups the
 * tail (`spendingBreakdown`), more slices would not be told apart. Slices go clockwise from the top with thin gaps; the
 * ring draws round the first time it shows. [content] sits in the hole (the total). The ring itself says nothing to
 * TalkBack beyond [description]: the list next to it carries the numbers.
 */
@Composable
fun CoShellDonut(
    slices: List<DonutSlice>,
    description: String,
    modifier: Modifier = Modifier,
    size: Dp = DONUT_SIZE,
    thickness: Dp = DONUT_THICKNESS,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val arcs = remember(slices) { donutArcs(slices.map { it.value }) }
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    // A pale slice on a pale card gets a hairline of its own deep shade (owner 2026-11-20), as every fill does.
    val surface = MaterialTheme.colorScheme.surface
    val edges = remember(slices, surface) { slices.map { fillEdgeColor(it.color, surface) } }
    val hairline = Sizes.hairline
    val entrance = rememberChartEntrance()
    val swap = remember { DonutSwap(DonutRing(arcs, slices.map { it.color }, edges)) }
    LaunchedEffect(arcs, edges) { swap.moveTo(DonutRing(arcs, slices.map { it.color }, edges)) }
    Box(modifier.size(size).semantics { contentDescription = description }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = thickness.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(inset, inset)
            // The whole ring opens clockwise: a slice is drawn up to where the sweep has reached.
            val reached = FULL * entrance.value
            fun ring(ring: DonutRing, arcs: List<DonutArc>, alpha: Float) {
                if (alpha <= 0f) return
                if (arcs.none { it.sweep > 0f }) {
                    drawArc(track, 0f, FULL, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke), alpha = alpha)
                    return
                }
                arcs.forEachIndexed { index, arc ->
                    val visible = (reached - arc.start).coerceIn(0f, arc.sweep)
                    if (visible > 0f) {
                        drawArc(ring.colors[index], arc.start + TOP, visible, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke), alpha = alpha)
                        ring.edges[index]?.let { drawSliceEdge(it.copy(alpha = it.alpha * alpha), arc.start + TOP, visible, stroke, hairline.toPx()) }
                    }
                }
            }
            // Other numbers (a period, a currency): the same slices turn to their new sizes; other slices — the old ring fades into the new (owner 2026-11-27).
            val t = swap.progress
            val leaving = swap.leaving
            when {
                leaving == null -> ring(swap.shown, swap.shown.arcs, 1f)
                leaving.sameSlices(swap.shown) -> ring(swap.shown, lerpArcs(leaving.arcs, swap.shown.arcs, t), 1f)
                else -> {
                    ring(leaving, leaving.arcs, 1f - t)
                    ring(swap.shown, swap.shown.arcs, t)
                }
            }
        }
        // The centre stays inside the hole: a long amount shrinks or wraps there instead of running over the ring (#554).
        Box(Modifier.size((size - thickness * 2) * HOLE_SHARE), contentAlignment = Alignment.Center, content = content)
    }
}

/** What one ring draws: its arcs and, per arc, the colour and the hairline. */
@Immutable
internal class DonutRing(val arcs: List<DonutArc>, val colors: List<Color>, val edges: List<Color?>) {
    /** The same categories in the same order — the arcs can turn instead of fading. */
    fun sameSlices(other: DonutRing): Boolean = arcs.isNotEmpty() && other.arcs.isNotEmpty() && colors == other.colors
}

/** The ring shown and the one leaving it, with how far the change has gone (0..1). Read in the draw lambda only. */
@Stable
internal class DonutSwap(first: DonutRing) {
    var shown by mutableStateOf(first)
        private set
    var leaving by mutableStateOf<DonutRing?>(null)
        private set
    var progress by mutableFloatStateOf(1f)
        private set

    suspend fun moveTo(ring: DonutRing) {
        if (ring.arcs == shown.arcs && ring.colors == shown.colors && ring.edges == shown.edges) return
        // Interrupted half way: start from what is on screen now, not from the end of the old move.
        val before = leaving
        leaving = if (before != null && before.sameSlices(shown)) DonutRing(lerpArcs(before.arcs, shown.arcs, progress), shown.colors, shown.edges)
        else if (before != null && progress < HALF) before else shown
        shown = ring
        progress = 0f
        animate(0f, 1f, animationSpec = Motion.spatial()) { value, _ -> progress = value }
        leaving = null
    }
}

/** Pure: arcs on their way from [from] to [to] (same count), [t] 0..1. */
internal fun lerpArcs(from: List<DonutArc>, to: List<DonutArc>, t: Float): List<DonutArc> =
    to.mapIndexed { i, arc ->
        val start = from.getOrNull(i) ?: arc
        DonutArc(start.start + (arc.start - start.start) * t, start.sweep + (arc.sweep - start.sweep) * t)
    }

/** Part of the hole's diameter the centre may use: a line of text across the middle keeps clear of the ring. */
private const val HOLE_SHARE = 0.84f

/** Outline of one ring slice ([start], [sweep] in degrees, ring [stroke] wide), drawn inside it with a [width] line. */
private fun DrawScope.drawSliceEdge(color: Color, start: Float, sweep: Float, stroke: Float, width: Float) {
    val mid = size.minDimension / 2 - stroke / 2
    val outer = mid + stroke / 2 - width / 2
    val inner = mid - stroke / 2 + width / 2
    if (sweep >= FULL) {
        drawCircle(color, outer, center, style = Stroke(width))
        drawCircle(color, inner, center, style = Stroke(width))
        return
    }
    val path = Path().apply {
        arcTo(Rect(center, outer), start, sweep, forceMoveTo = true)
        arcTo(Rect(center, inner), start + sweep, -sweep, forceMoveTo = false)
        close()
    }
    drawPath(path, color, style = Stroke(width))
}

/**
 * Pure: the arcs of [values] (shares of their sum), each [gap] degrees shorter so slices stand apart; a slice too small
 * for that still shows as a sliver of [MIN_SWEEP]. One slice — a full ring without a gap; nothing positive — no arcs.
 */
fun donutArcs(values: List<Long>, gap: Float = GAP): List<DonutArc> {
    val total = values.sumOf { it.coerceAtLeast(0) }
    if (total <= 0) return emptyList()
    if (values.count { it > 0 } == 1) return values.map { if (it > 0) DonutArc(0f, FULL) else DonutArc(0f, 0f) }
    var start = 0f
    return values.map { value ->
        val whole = FULL * value.coerceAtLeast(0) / total
        val arc = DonutArc(start + gap / 2, if (whole <= 0f) 0f else (whole - gap).coerceAtLeast(MIN_SWEEP))
        start += whole
        arc
    }
}

/** Categories on a ring before the rest goes to «Другое» (`docs/research/reports-and-charts.md`). */
const val DONUT_MAX_SLICES = 6

private const val FULL = 360f
private const val HALF = 0.5f
/** Canvas angles start at 3 o'clock; ours at 12. */
private const val TOP = -90f
private const val GAP = 2f
private const val MIN_SWEEP = 0.8f
private val DONUT_SIZE = 140.dp
private val DONUT_THICKNESS = 16.dp

package app.coshell.core.designsystem.charts

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.coshell.core.designsystem.tokens.Motion
import kotlin.math.floor

/**
 * The points of a chart that move to new data instead of jumping (owner 2026-11-27: switching data in report cards is
 * smooth): every point travels from where it is now to its new height on [Motion.spatial]. A different number of points
 * (30 → 60 days) is read along the old line, so the line stretches rather than blinks. Read [at] and [size] only in a draw
 * lambda — frames redraw without recomposing.
 */
@Stable
internal class ChartMorph(initial: List<Float>) {
    private var from by mutableStateOf(initial)
    private var to by mutableStateOf(initial)
    private var progress by mutableFloatStateOf(1f)

    /** How many points are drawn now (the new count as soon as the move starts). */
    val size: Int get() = to.size

    /** The height of point [i] (of [size]) at this frame. */
    fun at(i: Int): Float {
        val target = to[i]
        if (from.isEmpty()) return target
        val start = sampleAlong(from, i, to.size)
        return start + (target - start) * progress
    }

    /** The height at a fractional index [x] (0..[size]−1): the selected dot sliding between points. */
    fun atFraction(x: Float): Float {
        if (size == 0) return 0f
        val lo = floor(x).toInt().coerceIn(0, size - 1)
        val hi = (lo + 1).coerceAtMost(size - 1)
        return at(lo) + (at(hi) - at(lo)) * (x - lo).coerceIn(0f, 1f)
    }

    internal suspend fun moveTo(target: List<Float>) {
        if (target == to) return
        // No suspension between the three writes: a frame sees either the old move or the start of the new one.
        from = List(to.size) { at(it) }
        to = target
        progress = 0f
        animate(0f, 1f, animationSpec = Motion.spatial()) { value, _ -> progress = value }
    }
}

/** Heights 0..1 that glide to [target] when it changes; the first time they are simply [target] (the entrance draws them). */
@Composable
internal fun rememberChartMorph(target: List<Float>): ChartMorph {
    val morph = remember { ChartMorph(target) }
    LaunchedEffect(target) { morph.moveTo(target) }
    return morph
}

/** Pure: the value at position [i] of [count] read along [values] (linear between neighbours, ends to ends). */
internal fun sampleAlong(values: List<Float>, i: Int, count: Int): Float {
    if (values.size == 1 || count <= 1) return values.first()
    val x = i.toFloat() * (values.size - 1) / (count - 1)
    val lo = floor(x).toInt().coerceIn(0, values.lastIndex)
    val hi = (lo + 1).coerceAtMost(values.lastIndex)
    return values[lo] + (values[hi] - values[lo]) * (x - lo)
}

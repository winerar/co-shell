package app.coshell.core.designsystem.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import app.coshell.core.designsystem.tokens.Motion

/**
 * One scale for every mark of a chart, bars start at zero (`docs/research/reports-and-charts.md`): the largest value —
 * a bar or a ghost of the past period — is the full length. Values are plain magnitudes (minor units of one currency).
 */
class ChartScale private constructor(private val max: Long) {
    /** [value] as a share of the largest, 0..1; a negative is 0. */
    fun of(value: Long): Float = if (max <= 0) 0f else (value.coerceIn(0, max).toDouble() / max).toFloat()

    companion object {
        /** The scale of [values]; `null` items (no ghost) are skipped. */
        fun of(values: Iterable<Long?>): ChartScale = ChartScale(values.filterNotNull().maxOrNull() ?: 0)
    }
}

/**
 * 0 → 1 the first time a chart shows (bars grow from the axis, the ring draws round), then stays 1: coming back to the
 * screen or rotating it does not replay. Read `value` only in a draw or placement lambda, so frames do not recompose.
 */
@Composable
internal fun rememberChartEntrance(): Animatable<Float, *> {
    val shown = rememberSaveable { booleanArrayOf(false) }
    val grow = remember { Animatable(if (shown[0]) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (shown[0]) return@LaunchedEffect
        shown[0] = true
        grow.animateTo(1f, Motion.spatialSlow())
    }
    return grow
}

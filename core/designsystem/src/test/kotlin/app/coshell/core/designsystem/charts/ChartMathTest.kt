package app.coshell.core.designsystem.charts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartMathTest {
    @Test
    fun `the largest value, a ghost too, is the full length and bars start at zero`() {
        val scale = ChartScale.of(listOf(290L, 300L, null, 121L))
        assertEquals(1f, scale.of(300), 0.0001f)
        assertEquals(290f / 300, scale.of(290), 0.0001f)
        assertEquals(0f, scale.of(-5), 0.0001f)
        assertEquals(1f, scale.of(400), 0.0001f)
    }

    @Test
    fun `nothing to show gives zeros`() {
        assertEquals(0f, ChartScale.of(listOf(0L, null)).of(10), 0.0001f)
        assertEquals(0f, ChartScale.of(emptyList()).of(0), 0.0001f)
    }

    @Test
    fun `arcs follow the shares with gaps and go round once`() {
        val arcs = donutArcs(listOf(50, 25, 25), gap = 2f)
        assertEquals(listOf(1f, 181f, 271f), arcs.map { it.start })
        assertEquals(listOf(178f, 88f, 88f), arcs.map { it.sweep })
        assertEquals(360f, arcs.last().start + arcs.last().sweep + 1f, 0.001f)
    }

    @Test
    fun `a tiny slice stays a sliver, a zero one is not drawn`() {
        val arcs = donutArcs(listOf(10_000, 1, 0), gap = 2f)
        assertTrue(arcs[1].sweep > 0f)
        assertEquals(0f, arcs[2].sweep, 0f)
    }

    @Test
    fun `one slice is a whole ring, nothing positive is no ring`() {
        assertEquals(listOf(DonutArc(0f, 360f), DonutArc(0f, 0f)), donutArcs(listOf(5, 0)))
        assertEquals(emptyList<DonutArc>(), donutArcs(listOf(0, -3)))
    }
}

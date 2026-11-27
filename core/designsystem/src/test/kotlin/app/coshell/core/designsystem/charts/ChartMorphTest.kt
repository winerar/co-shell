package app.coshell.core.designsystem.charts

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pure parts of switching chart data smoothly (owner 2026-11-27): where the old line is read, the forecast marks, the turning ring. */
class ChartMorphTest {
    @Test
    fun `same count reads the same points`() {
        val old = listOf(0f, 0.5f, 1f)
        assertEquals(listOf(0f, 0.5f, 1f), (0..2).map { sampleAlong(old, it, 3) })
    }

    @Test
    fun `more points are read along the old line end to end`() {
        val old = listOf(0f, 1f)
        assertEquals(listOf(0f, 0.25f, 0.5f, 0.75f, 1f), (0..4).map { sampleAlong(old, it, 5) })
    }

    @Test
    fun `one old point holds every new one`() {
        assertEquals(0.4f, sampleAlong(listOf(0.4f), 3, 7), D)
    }

    @Test
    fun `a new morph shows its target at once`() {
        val morph = ChartMorph(listOf(0.2f, 0.8f))
        assertEquals(2, morph.size)
        assertEquals(0.2f, morph.at(0), D)
        assertEquals(0.5f, morph.atFraction(0.5f), D)
    }

    @Test
    fun `forecast shape keeps zero on the scale and places today and the lowest day`() {
        val shape = forecastShape(past = listOf(100L), future = listOf(100L, 50L, 300L), lowest = 1)
        assertEquals(listOf(1f / 3, 1f / 3, 1f / 6, 1f), shape.heights)
        assertEquals(0f, shape.zero, D)
        assertEquals(1f / 3, shape.today, D)
        assertEquals(2f / 3, shape.lowest, D)
    }

    @Test
    fun `forecast shape below zero lifts the zero line`() {
        val shape = forecastShape(past = emptyList(), future = listOf(100L, -100L), lowest = 1)
        assertEquals(0.5f, shape.zero, D)
        assertEquals(listOf(1f, 0f), shape.heights)
    }

    @Test
    fun `no future draws nothing`() {
        assertEquals(emptyList<Float>(), forecastShape(listOf(1L), emptyList(), 0).heights)
    }

    @Test
    fun `arcs turn half way`() {
        val from = listOf(DonutArc(0f, 100f), DonutArc(100f, 260f))
        val to = listOf(DonutArc(0f, 200f), DonutArc(200f, 160f))
        assertEquals(listOf(DonutArc(0f, 150f), DonutArc(150f, 210f)), lerpArcs(from, to, 0.5f))
    }
}

private const val D = 1e-6f

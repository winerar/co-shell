package app.coshell.core.designsystem.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** #796: «+» stays in the middle, the selected tab borrows room only from its neighbour in its own half. */
class BarWidthsTest {
    private val min = 48.dp

    private fun widths(selected: Int, inner: Float): List<Float> {
        val slot = BarWidths.addSlot(inner.dp, 4, 64.dp)
        return BarWidths.of(4, selected, inner.dp, slot, min).map { it.value }
    }

    /** Left half + «+» slot + right half: the left tabs end exactly where the «+» starts, whatever is selected. */
    private fun assertCentered(inner: Float) {
        val slot = BarWidths.addSlot(inner.dp, 4, 64.dp).value
        for (selected in -1..3) {
            val w = widths(selected, inner)
            assertEquals("halves differ, selected $selected", w[0] + w[1], w[2] + w[3], 0.01f)
            assertEquals("bar not filled, selected $selected", inner, w.sum() + slot, 0.01f)
        }
    }

    @Test
    fun `the plus never leaves the middle on any bar width`() {
        listOf(244f, 314f, 400f, 700f).forEach(::assertCentered)
    }

    @Test
    fun `the selected tab takes the room its neighbour gives up`() {
        val w = widths(selected = 1, inner = 314f)
        assertEquals(48f, w[0], 0.01f)
        assertTrue(w[1] > 70f)
        assertEquals(w[2], w[3], 0.01f)
    }

    @Test
    fun `the other half stays equal`() {
        val w = widths(selected = 0, inner = 314f)
        assertEquals(w[2], w[3], 0.01f)
    }

    @Test
    fun `nothing selected means equal tabs`() {
        val w = widths(selected = -1, inner = 314f)
        assertEquals(w.first(), w.last(), 0.01f)
        assertEquals(w[1], w[2], 0.01f)
    }

    @Test
    fun `no tab goes below a touch target on a narrow bar`() {
        for (selected in -1..3) assertTrue(widths(selected, 244f).all { it >= 47.9f })
    }

    @Test
    fun `a word is shown only when it fits with margins`() {
        assertTrue(BarWidths.fits(50.dp, 77.dp, 8.dp))
        assertTrue(!BarWidths.fits(50.dp, 49.dp, 8.dp))
    }

    @Test
    fun `spans put the pill in each final slot with the plus between the halves`() {
        val spans = BarWidths.spans(listOf(100, 48, 74, 74), 64, 2)
        assertEquals(listOf(2 to 98, 102 to 146, 214 to 284, 288 to 358), spans)
    }

    @Test
    fun `spans stop where a row that rounded past the bar squeezes its last cell`() {
        assertEquals(288 to 357, BarWidths.spans(listOf(100, 48, 74, 75), 64, 2, total = 359).last())
    }
}

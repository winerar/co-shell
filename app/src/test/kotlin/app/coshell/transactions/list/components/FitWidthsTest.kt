package app.coshell.transactions.list.components

import org.junit.Assert.assertEquals
import org.junit.Test

class FitWidthsTest {
    @Test
    fun `everyone keeps its width when all fit`() = assertEquals(listOf(90, 48), fitWidths(listOf(90, 48), 166))

    @Test
    fun `only the wider one shrinks when space runs short`() = assertEquals(listOf(70, 48), fitWidths(listOf(90, 48), 118))

    @Test
    fun `both wide ones share the space evenly`() = assertEquals(listOf(60, 60), fitWidths(listOf(132, 120), 120))

    @Test
    fun `no space gives zero widths`() = assertEquals(listOf(0, 0), fitWidths(listOf(90, 48), -5))
}

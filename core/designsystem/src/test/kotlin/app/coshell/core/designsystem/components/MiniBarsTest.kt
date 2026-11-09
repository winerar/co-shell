package app.coshell.core.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Test

class MiniBarsTest {
    @Test
    fun `values become shares of the largest`() {
        assertEquals(listOf(0.5f, 1f, 0f), miniBarFractions(listOf(50, 100, 0)))
    }

    @Test
    fun `all zeros stay flat instead of dividing by zero`() {
        assertEquals(listOf(0f, 0f), miniBarFractions(listOf(0, 0)))
    }

    @Test
    fun `negative values count as zero`() {
        assertEquals(listOf(0f, 1f), miniBarFractions(listOf(-10, 20)))
    }

    @Test
    fun `no values give no bars`() {
        assertEquals(emptyList<Float>(), miniBarFractions(emptyList()))
    }
}

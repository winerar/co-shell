package app.coshell.core.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RowGroupTest {
    @Test
    fun `a single row is both first and last`() {
        val only = RowGroupPosition(0, 1)
        assertTrue(only.isFirst)
        assertTrue(only.isLast)
    }

    @Test
    fun `a middle row is neither first nor last`() {
        val middle = RowGroupPosition(1, 3)
        assertFalse(middle.isFirst)
        assertFalse(middle.isLast)
        assertTrue(RowGroupPosition(2, 3).isLast)
    }

    @Test
    fun `scope keeps only the rows actually added`() {
        val shown = false
        val scope = RowGroupScope().apply {
            row {}
            if (shown) row {}
            row {}
        }
        assertEquals(2, scope.rows.size)
    }
}

package app.coshell.core.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Test

class CloseOnceTest {
    @Test
    fun `sheet reports closing only once when swiped away after save`() {
        var closed = 0
        val once = CloseOnce { closed++ }
        once.close() // «Сохранить» → slide down finished
        once.close() // the dismiss request that follows
        assertEquals(1, closed)
    }
}

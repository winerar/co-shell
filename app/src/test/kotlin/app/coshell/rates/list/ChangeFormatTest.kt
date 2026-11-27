package app.coshell.rates.list

import org.junit.Assert.assertEquals
import org.junit.Test

class ChangeFormatTest {
    @Test
    fun `change is written in tenths of a percent with a real minus`() {
        assertEquals("−6,1 %", formatChangeBp(-610))
        assertEquals("+1,2 %", formatChangeBp(120))
        assertEquals("0,0 %", formatChangeBp(0))
        assertEquals("0,0 %", formatChangeBp(-4))
        assertEquals("+12,5 %", formatChangeBp(1249))
    }
}

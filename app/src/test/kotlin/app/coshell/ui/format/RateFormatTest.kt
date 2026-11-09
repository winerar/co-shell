package app.coshell.ui.format

import app.coshell.core.model.Rate
import org.junit.Assert.assertEquals
import org.junit.Test

class RateFormatTest {
    private fun format(text: String) = formatRate(Rate.parse(text))

    @Test
    fun `large rates keep two fraction digits with grouping`() {
        assertEquals("1 049,69", format("1049.6912"))
        assertEquals("1 460", format("1460"))
    }

    @Test
    fun `rates from one keep four fraction digits`() {
        assertEquals("1,0846", format("1.08456"))
        assertEquals("2,5", format("2.5"))
    }

    @Test
    fun `rates below one keep four significant digits`() {
        assertEquals("0,0006849", format("0.000684931506849315"))
        assertEquals("0,8621", format("0.86206896"))
    }

    @Test
    fun `tiny rates are not rounded to zero`() {
        // 1 USD in BTC: used to crash the "Rates" screen (rounded to 0 → "Rate must be positive").
        assertEquals("0,00000987", format("0.00000987"))
        assertEquals("0,00001012", format("0.0000101234"))
        assertEquals("0,000000000000000001", format("0.000000000000000001"))
    }
}

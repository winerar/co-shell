package app.coshell.core.designsystem.amount

import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class FitAmountSizeTest {
    @Test
    fun fittingTextKeepsItsSize() {
        assertEquals(14.sp, shrunkToFit(14.sp, naturalPx = 200, widthPx = 200))
        assertEquals(14.sp, shrunkToFit(14.sp, naturalPx = 120, widthPx = 200))
    }

    @Test
    fun tooWideTextShrinksInProportion() {
        // «999 999 999,99» in a 320 dp tile at ×1.3: twice too wide at the minimum → half the size, not cut.
        assertEquals(7f, shrunkToFit(14.sp, naturalPx = 400, widthPx = 200).value, 0.001f)
    }

    @Test
    fun neverBelowTheFloor() {
        assertEquals(FLOOR, shrunkToFit(14.sp, naturalPx = 10_000, widthPx = 100))
    }

    @Test
    fun sameBoundsAreEqual() {
        assertEquals(fitAmountAutoSize(9.sp, 16.sp), fitAmountAutoSize(9.sp, 16.sp))
        assertEquals(fitAmountAutoSize(9.sp, 16.sp).hashCode(), fitAmountAutoSize(9.sp, 16.sp).hashCode())
    }
}

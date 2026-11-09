package app.coshell.core.designsystem.amount

import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class RollingAmountTest {
    private fun usd(n: String) = "$n USD"

    @Test
    fun directionFollowsTheValue() {
        assertEquals(1, amountDirection(usd("99,50"), usd("1 000,00")))
        assertEquals(-1, amountDirection(usd("1 000,00"), usd("999,99")))
        assertEquals(0, amountDirection(usd("5,00"), usd("5,00")))
    }

    @Test
    fun negativeAmountsAreSmaller() {
        assertEquals(-1, amountDirection(usd("10,00"), usd("−10,00")))
        assertEquals(1, amountDirection(usd("−20,00"), usd("−10,00")))
        assertEquals(-1, amountDirection(usd("−10,00"), usd("−20,00")))
    }

    @Test
    fun notAnAmountGivesZero() {
        assertEquals(0, amountDirection("—", usd("1,00")))
    }

    @Test
    fun sizeShrinksOnlyWhenNeeded() {
        assertEquals(40.sp, fittedSize(40.sp, widthPx = 1000f, ems = 10f, pxPerSp = 2f)) // 50 sp would fit
        assertEquals(25.sp, fittedSize(40.sp, widthPx = 500f, ems = 10f, pxPerSp = 2f))
    }

    @Test
    fun splitSeparatesTheCode() {
        assertEquals("1 234,50" to "USD", splitAmount(usd("1 234,50")))
        assertEquals("$5" to null, splitAmount("$5"))
    }
}

package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WideMathTest {

    @Test
    fun `multiplies beyond 64 bits exactly`() {
        val p = mulWide(Long.MAX_VALUE, Long.MAX_VALUE) // (2^63-1)^2 = 2^126 - 2^64 + 1
        assertEquals(U128((1uL shl 62) - 1uL, 1uL), p)
        assertEquals(38, digitCount(pow10Wide(37)))
        assertEquals(1, digitCount(U128.of(0)))
        assertEquals(19, digitCount(U128.of(Long.MAX_VALUE)))
    }

    @Test
    fun `divides with banker's rounding`() {
        assertEquals(U128.of(2), divRoundHalfEven(U128.of(5), 2)) // 2.5 → 2
        assertEquals(U128.of(4), divRoundHalfEven(U128.of(7), 2)) // 3.5 → 4
        assertEquals(U128.of(3), divRoundHalfEven(U128.of(8), 3)) // 2.67 → 3
        assertEquals(U128.of(2), divRoundHalfEven(U128.of(7), 3)) // 2.33 → 2
        // 128-bit numerator and divisor: 10^30 / 10^20 = 10^10
        assertEquals(U128.of(10_000_000_000), divRoundHalfEven(pow10Wide(30), pow10Wide(20)))
        // Divisor above 2^127 exercises the overflow branch of the shift.
        val big = U128(1uL shl 63, 0uL)
        assertEquals(U128.of(2), divRoundHalfEven(U128(ULong.MAX_VALUE, 0uL), big)) // 1.99… → 2
        assertEquals(U128.of(2), divRoundHalfEven(U128(3uL shl 62, 0uL), big)) // 1.5 → 2
        assertEquals(U128.of(1), divRoundHalfEven(U128(5uL shl 61, 0uL), big)) // 1.25 → 1
    }

    @Test
    fun `reports overflow`() {
        assertFailsWith<ArithmeticException> { mulWide(pow10Wide(38), 10) }
        assertFailsWith<ArithmeticException> { pow10Wide(19).toLongExact() }
    }
}

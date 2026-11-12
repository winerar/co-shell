package app.coshell.core.domain.reports

import kotlin.test.Test
import kotlin.test.assertEquals

class SharesTest {
    @Test
    fun `percents add up to 100 by the largest remainder`() {
        assertEquals(listOf(34, 33, 33), percentShares(listOf(1, 1, 1)))
        assertEquals(listOf(67, 33), percentShares(listOf(2, 1)))
        assertEquals(listOf(50, 25, 25), percentShares(listOf(200, 100, 100)))
    }

    @Test
    fun `non-positive parts get nothing and are not in the sum`() {
        assertEquals(listOf(100, 0, 0), percentShares(listOf(5, 0, -3)))
        assertEquals(listOf(0, 0), percentShares(listOf(0, -1)))
    }

    @Test
    fun `huge amounts do not overflow`() {
        assertEquals(listOf(50, 50), percentShares(listOf(Long.MAX_VALUE / 2, Long.MAX_VALUE / 2)))
    }
}

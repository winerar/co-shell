package app.coshell.core.designsystem.tokens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TokensTest {
    @Test
    fun spacingIsIncreasingAndOnFourDpGrid() {
        val values = Spacing.scale.map { it.value }
        assertEquals(values.sorted(), values)
        assertEquals(values.size, values.toSet().size)
        // Only the hairline step is allowed off the 4 dp grid.
        values.drop(1).forEach { assertEquals("$it dp is off the 4 dp grid", 0f, it % 4f) }
    }

    @Test
    fun radiiGrowFromSmallToExtraLarge() {
        val values = listOf(Radii.small, Radii.medium, Radii.large, Radii.extraLarge).map { it.value }
        assertEquals(values.sorted(), values)
    }

    @Test
    fun motionDurationsAreShortAndOrdered() {
        assertTrue(Motion.SHORT_MS < Motion.MEDIUM_MS)
        assertTrue(Motion.MEDIUM_MS < Motion.LONG_MS)
        // Long transitions make the app feel slow; keep every token under 400 ms.
        assertTrue(Motion.LONG_MS <= 400)
    }
}

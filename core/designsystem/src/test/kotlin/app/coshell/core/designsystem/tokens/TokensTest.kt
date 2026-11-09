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
    fun motionSpringsAreOrderedFastToSlow() {
        assertTrue(Motion.SPATIAL_FAST > Motion.SPATIAL_DEFAULT && Motion.SPATIAL_DEFAULT > Motion.SPATIAL_SLOW)
        assertTrue(Motion.EFFECTS_FAST > Motion.EFFECTS_DEFAULT && Motion.EFFECTS_DEFAULT > Motion.EFFECTS_SLOW)
        // Effects never overshoot; spatial lands softly; only "bouncy" visibly springs past the target.
        assertEquals(1f, Motion.EFFECTS_DAMPING)
        assertTrue(Motion.SPATIAL_DAMPING in 0.8f..1f)
        assertTrue(Motion.BOUNCY_DAMPING < Motion.SPATIAL_DAMPING)
        // Slowest spatial spring settles (≈ 4 / (ζ·ω)) well under half a second: the app stays snappy.
        val settleSeconds = 4.0 / (Motion.SPATIAL_DAMPING * Math.sqrt(Motion.SPATIAL_SLOW.toDouble()))
        assertTrue("slow spring settles in $settleSeconds s", settleSeconds < 0.5)
    }

    @Test
    fun sizesSitOnTheFourDpGrid() {
        listOf(Sizes.rowMinHeight, Sizes.badge, Sizes.badgeSmall, Sizes.fabClearance).forEach {
            assertEquals("${it.value} dp is off the 4 dp grid", 0f, it.value % 4f)
        }
        assertTrue(Sizes.badgeSmall < Sizes.badge)
    }
}

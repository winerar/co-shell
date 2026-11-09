package app.coshell.core.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SheetMotionTest {
    @Test
    fun dragFollowsTheFingerOneToOneAndStaysOnScreen() {
        assertEquals(0.25f, draggedHidden(0f, 250f, 1000f), 0.0001f)
        assertEquals(0f, draggedHidden(0.1f, -500f, 1000f), 0f) // never above fully open
        assertEquals(1f, draggedHidden(0.9f, 500f, 1000f), 0f)
        assertEquals(0.3f, draggedHidden(0.3f, 100f, 0f), 0f) // not measured yet — no move
    }

    @Test
    fun releaseDecidesByPositionUnlessFlung() {
        assertFalse(shouldClose(0.2f, 0f))
        assertTrue(shouldClose(0.5f, 0f))
        assertTrue(shouldClose(0.05f, 3f)) // quick flick down closes even near the top
        assertFalse(shouldClose(0.8f, -3f)) // flick up keeps it open even when low
    }

    @Test
    fun scaleGrowsWhileRisingAndShrinksWithBackGesture() {
        assertEquals(OPEN_FROM_SCALE, sheetScale(1f, 0f), 0.0001f)
        assertEquals(1f, sheetScale(0f, 0f), 0.0001f)
        assertEquals(1f - BACK_SHRINK, sheetScale(0f, 1f), 0.0001f)
    }

    @Test
    fun scrimFollowsTheSheet() {
        assertEquals(0f, scrimFraction(1f), 0f)
        assertEquals(1f, scrimFraction(0f), 0f)
        assertEquals(0.6f, scrimFraction(0.4f), 0.0001f)
    }
}

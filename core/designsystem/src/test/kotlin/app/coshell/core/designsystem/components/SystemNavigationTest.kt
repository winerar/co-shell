package app.coshell.core.designsystem.components

import app.coshell.core.designsystem.tokens.Spacing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phones with navigation buttons: the floating capsules sit closer to the button band, which already stands off the edge. */
class SystemNavigationTest {
    @Test
    fun `with gestures the capsule keeps the usual gap`() = assertEquals(Spacing.l, capsuleBottomGap(navigationButtons = false))

    @Test
    fun `with navigation buttons the gap is smaller`() {
        assertEquals(Spacing.s, capsuleBottomGap(navigationButtons = true))
        assertTrue(capsuleBottomGap(true) < capsuleBottomGap(false))
    }
}

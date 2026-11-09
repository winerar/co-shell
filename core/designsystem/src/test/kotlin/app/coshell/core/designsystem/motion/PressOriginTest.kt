package app.coshell.core.designsystem.motion

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PressOriginTest {
    private var now = 0L
    private val store = PressOriginStore { now }
    private val tap = PressPoint(Offset(10f, 20f))

    @Test
    fun recentTapIsTakenOnce() {
        store.record(tap)
        now = 300
        assertEquals(tap, store.take())
        assertNull(store.take()) // one tap opens one thing
    }

    @Test
    fun oldTapIsIgnored() {
        store.record(tap)
        now = PressOriginStore.MAX_AGE_MS + 1
        assertNull(store.take())
    }

    @Test
    fun clearedTapIsGone() {
        store.record(tap)
        store.clear()
        assertNull(store.take())
    }

    @Test
    fun originIsFractionOfBoundsAndClamped() {
        val bounds = Rect(100f, 0f, 300f, 400f)
        assertEquals(TransformOrigin(0.5f, 0.25f), transformOriginIn(bounds, Offset(200f, 100f)))
        assertEquals(TransformOrigin(0f, 1f), transformOriginIn(bounds, Offset(0f, 900f)))
        assertEquals(TransformOrigin.Center, transformOriginIn(Rect.Zero, Offset(5f, 5f)))
    }

    @Test
    fun openerColourHoldsThenDissolves() {
        assertEquals(1f, colorTintAlpha(0f), 0f)
        assertEquals(1f, colorTintAlpha(0.1f), 0f)
        assertEquals(0.5f, colorTintAlpha(0.45f), 0.01f)
        assertEquals(0f, colorTintAlpha(0.8f), 0f)
        assertEquals(0f, colorTintAlpha(1f), 0f)
    }
}

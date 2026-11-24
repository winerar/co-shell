package app.coshell.core.designsystem.components

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionFanGeometryTest {
    @Test
    fun `three buttons stand left, up and right of the plus`() {
        val left = ActionFanGeometry.center(0, 3, 100f)
        val up = ActionFanGeometry.center(1, 3, 100f)
        val right = ActionFanGeometry.center(2, 3, 100f)
        assertTrue(left.x < 0 && right.x > 0)
        assertEquals(0f, up.x, 0.001f)
        assertEquals(-100f, up.y, 0.001f)
        assertEquals(-left.x, right.x, 0.001f)
        assertEquals(left.y, right.y, 0.001f)
    }

    @Test
    fun `a lone button stands straight up`() {
        assertEquals(Offset(0f, -80f).y, ActionFanGeometry.center(0, 1, 80f).y, 0.001f)
        assertEquals(0f, ActionFanGeometry.center(0, 1, 80f).x, 0.001f)
    }

    @Test
    fun `the finger picks the nearest button within reach and nothing far from them`() {
        val target = ActionFanGeometry.center(2, 3, 100f)
        assertEquals(2 as Int?, ActionFanGeometry.hit(target + Offset(10f, 5f), 3, 100f, 40f))
        assertEquals(1 as Int?, ActionFanGeometry.hit(Offset(0f, -90f), 3, 100f, 40f))
        assertNull(ActionFanGeometry.hit(Offset(0f, 0f), 3, 100f, 40f))
        assertNull(ActionFanGeometry.hit(Offset(0f, -300f), 3, 100f, 40f))
    }
}

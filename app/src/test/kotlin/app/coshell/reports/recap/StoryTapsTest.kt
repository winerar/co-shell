package app.coshell.reports.recap

import org.junit.Assert.assertEquals
import org.junit.Test

class StoryTapsTest {
    private fun step(downX: Float, dragX: Float = 0f) = storyStep(downX, dragX, width = 300f, swipe = 80f)

    @Test
    fun `a tap on the left third goes back and elsewhere goes on`() {
        assertEquals(-1, step(downX = 10f))
        assertEquals(-1, step(downX = 99f))
        assertEquals(1, step(downX = 100f))
        assertEquals(1, step(downX = 290f))
    }

    @Test
    fun `a swipe right goes back and a swipe left goes on wherever it starts`() {
        assertEquals(-1, step(downX = 250f, dragX = 80f))
        assertEquals(1, step(downX = 20f, dragX = -120f))
    }

    @Test
    fun `a small slide is still a tap`() {
        assertEquals(1, step(downX = 200f, dragX = 40f))
        assertEquals(-1, step(downX = 50f, dragX = -40f))
    }
}

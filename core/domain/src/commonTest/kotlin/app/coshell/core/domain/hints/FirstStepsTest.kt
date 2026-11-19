package app.coshell.core.domain.hints

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FirstStepsTest {
    private fun state(a: Boolean = false, e: Boolean = false, l: Boolean = false, p: Boolean = false, hidden: Boolean = false) =
        FirstStepsState.of(FirstStepsData(a, e, l, p), hidden)

    @Test fun `empty app starts with the account`() {
        val s = state()
        assertEquals(0, s.doneCount)
        assertEquals(FirstStep.ACCOUNT, s.next)
        assertTrue(s.visible)
    }

    @Test fun `steps are ticked by data`() {
        val s = state(a = true, e = true)
        assertEquals(2, s.doneCount)
        assertEquals(FirstStep.LIMIT, s.next)
    }

    @Test fun `family stays optional for completion`() {
        val s = state(a = true, e = true, l = true)
        assertTrue(s.complete)
        assertFalse(s.visible)
        assertEquals(FirstStep.FAMILY, s.next)
    }

    @Test fun `all done has no next`() = assertNull(state(true, true, true, true).next)

    @Test fun `hidden steps are not shown`() = assertFalse(state(hidden = true).visible)
}

package app.coshell.core.domain.hints

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StepsMomentsTest {
    private val empty = FirstStepsData(hasAccount = false, hasEntry = false, hasLimit = false, hasPartner = false)

    @Test fun `the first entry is a moment`() =
        assertEquals(StepsMoment.FIRST_ENTRY, StepsMoments.between(empty.copy(hasAccount = true), empty.copy(hasAccount = true, hasEntry = true)))

    @Test fun `the first limit is a moment when the steps are not finished by it`() {
        val before = empty.copy(hasEntry = true)
        assertEquals(StepsMoment.FIRST_LIMIT, StepsMoments.between(before, before.copy(hasLimit = true)))
    }

    @Test fun `finishing the steps is the finale and beats the single moments`() {
        val before = empty.copy(hasAccount = true, hasEntry = true)
        assertEquals(StepsMoment.FINALE, StepsMoments.between(before, before.copy(hasLimit = true)))
    }

    @Test fun `no change and no moment`() {
        assertNull(StepsMoments.between(empty, empty))
        val done = empty.copy(hasAccount = true, hasEntry = true, hasLimit = true)
        assertNull(StepsMoments.between(done, done))
        assertNull(StepsMoments.between(empty, empty.copy(hasAccount = true)))
    }
}

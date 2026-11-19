package app.coshell.core.domain.hints

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HintPlannerTest {
    private val all = setOf(HintId.ACCOUNTS, HintId.HOLD_SELECT)

    private fun next(
        candidates: Set<HintId> = all,
        enabled: Boolean = true,
        shown: Set<String> = emptySet(),
        last: Long? = null,
        visit: Boolean = false,
    ) = HintPlanner.next(candidates, enabled, shown, last, today = 100, shownThisVisit = visit)

    @Test fun `picks the first by priority`() = assertEquals(HintId.ACCOUNTS, next())

    @Test fun `skips what was shown`() = assertEquals(HintId.HOLD_SELECT, next(shown = setOf("accounts")))

    @Test fun `shows nothing when hints are off`() = assertNull(next(enabled = false))

    @Test fun `one per day`() {
        assertNull(next(last = 100))
        assertEquals(HintId.ACCOUNTS, next(last = 99))
    }

    @Test fun `one per visit`() = assertNull(next(visit = true))

    @Test fun `moments ignore the day and visit limits`() {
        assertEquals(HintId.FIRST_ENTRY, next(candidates = all + HintId.FIRST_ENTRY, last = 100, visit = true))
        assertNull(next(candidates = setOf(HintId.ACCOUNTS), last = 100, visit = true))
        assertNull(next(candidates = setOf(HintId.FIRST_ENTRY), last = 100, enabled = false))
    }

    @Test fun `nothing when no condition holds`() = assertNull(next(candidates = emptySet()))

    @Test fun `keys are unique and found again`() {
        assertEquals(HintId.entries.size, HintId.entries.map { it.key }.toSet().size)
        assertTrue(HintId.entries.all { HintId.fromKey(it.key) == it })
        assertTrue(HintId.retired.none { HintId.fromKey(it) != null })
    }
}

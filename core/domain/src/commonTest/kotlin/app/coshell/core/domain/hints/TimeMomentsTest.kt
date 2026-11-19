package app.coshell.core.domain.hints

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeMomentsTest {
    private fun facts(
        today: Long = 100,
        seen: Long = 100,
        firstEntry: Long? = null,
        lastEntry: Long? = null,
        count: Int = 0,
    ) = MomentFacts(today, seen, firstEntry, lastEntry, count)

    @Test fun `nothing on the first day`() = assertEquals(emptySet(), TimeMoments.due(facts()))

    @Test fun `theme after three days`() {
        assertEquals(emptySet(), TimeMoments.due(facts(seen = 98)))
        assertEquals(setOf(HintId.THEME), TimeMoments.due(facts(seen = 97)))
    }

    @Test fun `first week needs a week of entries`() {
        assertEquals(emptySet(), TimeMoments.due(facts(firstEntry = 94, lastEntry = 100, count = 5)))
        assertEquals(setOf(HintId.FIRST_WEEK), TimeMoments.due(facts(firstEntry = 93, lastEntry = 100, count = 5)))
    }

    @Test fun `welcome back after three days without entries`() {
        assertEquals(emptySet(), TimeMoments.due(facts(firstEntry = 99, lastEntry = 98, count = 2)))
        assertEquals(setOf(HintId.WELCOME_BACK), TimeMoments.due(facts(firstEntry = 99, lastEntry = 97, count = 2)))
        assertEquals(emptySet(), TimeMoments.due(facts(lastEntry = 90, count = 0)))
    }

    @Test fun `backup by count or by age with entries`() {
        assertEquals(setOf(HintId.BACKUP), TimeMoments.due(facts(count = 30)))
        assertEquals(emptySet(), TimeMoments.due(facts(count = 29)))
        assertEquals(setOf(HintId.BACKUP, HintId.THEME), TimeMoments.due(facts(seen = 86, count = 1, firstEntry = 100, lastEntry = 100)))
        assertEquals(setOf(HintId.THEME), TimeMoments.due(facts(seen = 86, count = 0)))
    }
}

package app.coshell.core.domain.notices

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class NoticePlannerTest {
    private val today = LocalDate(2026, 11, 25)
    private val all = NoticeKind.entries.toSet()

    private fun facts(
        hasEntries: Boolean = true,
        entryToday: Boolean = false,
        last: LocalDate? = today.minusDays(1),
        seen: LocalDate = today.minusDays(5),
        backup: LocalDate? = null,
        limit: LimitFact? = null,
        lastMonth: Boolean = false,
        drafts: Int = 0,
        day: LocalDate = today,
    ) = NoticeFacts(day, hasEntries, entryToday, last, seen, backup, limit, lastMonth, drafts)

    private fun LocalDate.minusDays(n: Int) = LocalDate.fromEpochDays(toEpochDays() - n)

    @Test fun `everything is off by default`() = assertNull(NoticePlanner.next(facts(), emptySet(), NoticeHistory()))

    @Test fun `evening asks when nothing was written today`() =
        assertEquals(NoticeKind.EVENING, NoticePlanner.next(facts(), all, NoticeHistory())?.kind)

    @Test fun `evening stays quiet after an entry today`() =
        assertNull(NoticePlanner.next(facts(entryToday = true), setOf(NoticeKind.EVENING), NoticeHistory()))

    @Test fun `evening stays quiet until there is a first entry`() =
        assertNull(NoticePlanner.next(facts(hasEntries = false, last = null), setOf(NoticeKind.EVENING), NoticeHistory()))

    @Test fun `at most one notice a day`() {
        val history = NoticeHistory(lastAnyDay = today)
        assertNull(NoticePlanner.next(facts(), all, history))
    }

    @Test fun `evening comes again the next day`() {
        val history = NoticeHistory(lastAnyDay = today.minusDays(1), perKind = mapOf(NoticeKind.EVENING to NoticeHistory.Shown(today.minusDays(1), "x")))
        assertEquals(NoticeKind.EVENING, NoticePlanner.next(facts(), all, history)?.kind)
    }

    @Test fun `limit wins over the evening reminder`() {
        val limit = LimitFact("food:2026-11", "Продукты", "18 000 ARS")
        val planned = NoticePlanner.next(facts(limit = limit), all, NoticeHistory())
        assertEquals(NoticeKind.LIMIT, planned?.kind)
        assertEquals(limit, planned?.limit)
    }

    @Test fun `the same limit is told once`() {
        val limit = LimitFact("food:2026-11", "Продукты", "18 000 ARS")
        val history = NoticeHistory(lastAnyDay = today.minusDays(1), perKind = mapOf(NoticeKind.LIMIT to NoticeHistory.Shown(today.minusDays(1), limit.key)))
        assertEquals(NoticeKind.EVENING, NoticePlanner.next(facts(limit = limit), all, history)?.kind)
    }

    @Test fun `another limit or a new month is told again`() {
        val old = NoticeHistory(lastAnyDay = today.minusDays(1), perKind = mapOf(NoticeKind.LIMIT to NoticeHistory.Shown(today.minusDays(1), "food:2026-10")))
        assertEquals(NoticeKind.LIMIT, NoticePlanner.next(facts(limit = LimitFact("food:2026-11", "Продукты", "1")), all, old)?.kind)
    }

    @Test fun `recap only in the first days of the month and once`() {
        val first = today.minusDays(24) // 2026-11-01
        assertEquals(NoticeKind.RECAP, NoticePlanner.next(facts(day = first, lastMonth = true, entryToday = true), all, NoticeHistory())?.kind)
        assertNull(NoticePlanner.next(facts(day = today, lastMonth = true, entryToday = true), setOf(NoticeKind.RECAP), NoticeHistory()))
        val told = NoticeHistory(lastAnyDay = first, perKind = mapOf(NoticeKind.RECAP to NoticeHistory.Shown(first, "2026-10")))
        assertNull(NoticePlanner.next(facts(day = first.minusDays(-1), lastMonth = true), setOf(NoticeKind.RECAP), told))
    }

    @Test fun `recap needs entries in the last month`() =
        assertNull(NoticePlanner.next(facts(day = LocalDate(2026, 12, 1), lastMonth = false), setOf(NoticeKind.RECAP), NoticeHistory()))

    @Test fun `comeback after three quiet days, once per silence`() {
        val away = facts(last = today.minusDays(3))
        val planned = NoticePlanner.next(away, setOf(NoticeKind.COMEBACK), NoticeHistory())
        assertEquals(NoticeKind.COMEBACK, planned?.kind)
        val told = NoticeHistory(lastAnyDay = today.minusDays(1), perKind = mapOf(NoticeKind.COMEBACK to NoticeHistory.Shown(today.minusDays(1), planned!!.key)))
        assertNull(NoticePlanner.next(away, setOf(NoticeKind.COMEBACK), told))
    }

    @Test fun `no comeback after two quiet days`() =
        assertNull(NoticePlanner.next(facts(last = today.minusDays(2)), setOf(NoticeKind.COMEBACK), NoticeHistory()))

    @Test fun `comeback takes the place of the evening reminder`() =
        assertEquals(NoticeKind.COMEBACK, NoticePlanner.next(facts(last = today.minusDays(4)), all, NoticeHistory())?.kind)

    @Test fun `backup after two weeks without a copy`() {
        assertNull(NoticePlanner.next(facts(seen = today.minusDays(13)), setOf(NoticeKind.BACKUP), NoticeHistory()))
        assertEquals(NoticeKind.BACKUP, NoticePlanner.next(facts(seen = today.minusDays(14)), setOf(NoticeKind.BACKUP), NoticeHistory())?.kind)
    }

    @Test fun `a recent copy keeps the backup quiet`() =
        assertNull(NoticePlanner.next(facts(seen = today.minusDays(60), backup = today.minusDays(3)), setOf(NoticeKind.BACKUP), NoticeHistory()))

    @Test fun `backup is not repeated for two weeks`() {
        val told = NoticeHistory(lastAnyDay = today.minusDays(5), perKind = mapOf(NoticeKind.BACKUP to NoticeHistory.Shown(today.minusDays(5), "x")))
        assertNull(NoticePlanner.next(facts(seen = today.minusDays(60)), setOf(NoticeKind.BACKUP), told))
        val long = NoticeHistory(lastAnyDay = today.minusDays(14), perKind = mapOf(NoticeKind.BACKUP to NoticeHistory.Shown(today.minusDays(14), "x")))
        assertEquals(NoticeKind.BACKUP, NoticePlanner.next(facts(seen = today.minusDays(60)), setOf(NoticeKind.BACKUP), long)?.kind)
    }

    @Test fun `drafts from three ready ones, not more often than every three days`() {
        assertNull(NoticePlanner.next(facts(drafts = 2), setOf(NoticeKind.DRAFTS), NoticeHistory()))
        assertEquals(NoticeKind.DRAFTS, NoticePlanner.next(facts(drafts = 3), setOf(NoticeKind.DRAFTS), NoticeHistory())?.kind)
        val told = NoticeHistory(lastAnyDay = today.minusDays(2), perKind = mapOf(NoticeKind.DRAFTS to NoticeHistory.Shown(today.minusDays(2), "x")))
        assertNull(NoticePlanner.next(facts(drafts = 5), setOf(NoticeKind.DRAFTS), told))
    }

    @Test fun `a switched off kind never goes out`() {
        val limit = LimitFact("k", "Продукты", "1")
        assertEquals(NoticeKind.EVENING, NoticePlanner.next(facts(limit = limit), setOf(NoticeKind.EVENING), NoticeHistory())?.kind)
    }

    @Test fun `keys round trip`() = NoticeKind.entries.forEach { assertEquals(it, NoticeKind.fromKey(it.key)) }
}

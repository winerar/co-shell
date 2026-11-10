package app.coshell.home.sync

import app.coshell.core.data.sync.SyncIssue
import app.coshell.core.data.sync.SyncIssueKind
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncBannerRulesTest {
    private val now = Instant.parse("2026-11-10T12:00:00Z")

    @Test
    fun `network hiccups wait for the grace period and need a family`() {
        val offline = SyncIssue(SyncIssueKind.OFFLINE, now - 90.seconds)
        assertFalse(SyncBannerRules.shouldShow(offline, now, inFamily = true, dismissed = null))
        assertTrue(SyncBannerRules.shouldShow(offline, now + 30.seconds, inFamily = true, dismissed = null))
        assertFalse(SyncBannerRules.shouldShow(offline, now + 5.minutes, inFamily = false, dismissed = null))
        assertFalse(SyncBannerRules.shouldShow(null, now, inFamily = true, dismissed = null))
    }

    @Test
    fun `problems that do not fix themselves show at once, even out of the family`() {
        for (kind in listOf(SyncIssueKind.SIGNED_OUT, SyncIssueKind.NO_ACCESS, SyncIssueKind.QUOTA)) {
            assertTrue(kind.name, SyncBannerRules.shouldShow(SyncIssue(kind, now), now, inFamily = false, dismissed = null))
        }
    }

    @Test
    fun `dismissed issue stays hidden until it changes`() {
        val quota = SyncIssue(SyncIssueKind.QUOTA, now, "RESOURCE_EXHAUSTED")
        assertFalse(SyncBannerRules.shouldShow(quota, now, inFamily = true, dismissed = quota))
        val tomorrow = quota.copy(since = now + 1.minutes)
        assertTrue(SyncBannerRules.shouldShow(tomorrow, now, inFamily = true, dismissed = quota))
    }

    @Test
    fun `quota resets at midnight in California, shown in local time`() {
        val buenosAires = TimeZone.of("America/Argentina/Buenos_Aires")
        // 12:00 UTC = 4:00 in California (PST, UTC−8) → next midnight PST = 08:00 UTC = 5:00 in Buenos Aires.
        assertEquals(LocalDateTime(2026, 11, 11, 5, 0), SyncBannerRules.quotaResetAt(now, buenosAires))
    }
}

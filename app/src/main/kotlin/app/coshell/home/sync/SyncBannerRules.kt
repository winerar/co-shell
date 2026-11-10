package app.coshell.home.sync

import app.coshell.core.data.sync.SyncIssue
import app.coshell.core.data.sync.SyncIssueKind
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** When the sync banner shows (note on board 17А). */
internal object SyncBannerRules {
    /** A short loss of network is normal on a phone: the banner waits this long. */
    val GRACE = 2.minutes

    /** Firestore's free daily quota resets at midnight Pacific time. */
    private val QUOTA_ZONE = TimeZone.of("America/Los_Angeles")

    /**
     * Sign-in, access and quota problems do not fix themselves: shown at once. Network and server
     * hiccups — only after [GRACE], and only while in a family. «Понятно» hides [dismissed] until
     * the problem changes.
     */
    fun shouldShow(issue: SyncIssue?, now: Instant, inFamily: Boolean, dismissed: SyncIssue?): Boolean = when {
        issue == null || issue == dismissed -> false
        issue.kind in setOf(SyncIssueKind.SIGNED_OUT, SyncIssueKind.NO_ACCESS, SyncIssueKind.QUOTA) -> true
        else -> inFamily && now - issue.since >= GRACE
    }

    /** The next quota reset in the phone's time zone. */
    fun quotaResetAt(now: Instant, zone: TimeZone): LocalDateTime {
        val tomorrow = now.toLocalDateTime(QUOTA_ZONE).date.plus(1, DateTimeUnit.DAY)
        return tomorrow.atStartOfDayIn(QUOTA_ZONE).toLocalDateTime(zone)
    }
}

package app.coshell.core.data.sync

import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Firestore Spark allows 20 000 writes a day (#130). When a push hits `RESOURCE_EXHAUSTED`, the
 * rows already sent stay sent, the rest stay pending, and sending pauses until the quota resets:
 * retrying earlier is useless and every poll would only waste a request. Pulling goes on.
 */
object QuotaPause {
    /** Key in `sync_state`: epoch ms until which [SyncEngine] does not push. */
    const val KEY = "push_paused_until"

    /** Firestore's free daily quota resets at midnight Pacific time. */
    val ZONE: TimeZone = TimeZone.of("America/Los_Angeles")

    /** The first quota reset after [now]. */
    fun nextReset(now: Instant): Instant {
        val tomorrow = now.toLocalDateTime(ZONE).date.plus(1, DateTimeUnit.DAY)
        return tomorrow.atStartOfDayIn(ZONE)
    }
}

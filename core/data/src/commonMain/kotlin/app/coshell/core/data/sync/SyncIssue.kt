package app.coshell.core.data.sync

import app.coshell.core.data.remote.ApiException
import kotlin.time.Instant

/** Why sync stopped working (feedback #102: «нет связи с сервером» said nothing useful). */
enum class SyncIssueKind {
    /** No network: [app.coshell.core.data.remote.HttpClient] throws only for network failures. */
    OFFLINE,

    /** Google sign-in expired: the server answered 401. */
    SIGNED_OUT,

    /** The family's rules let us in no more: 403 / PERMISSION_DENIED. */
    NO_ACCESS,

    /** Daily free quota is over: 429 / RESOURCE_EXHAUSTED. */
    QUOTA,

    /** Any other server answer. */
    SERVER,
}

/**
 * The last sync failure, kept in `sync_state` under [KEY] until a sync succeeds, so «Обзор» reads it
 * from Room like everything else. [since] — when this kind of failure started (repeats keep it);
 * [code] — the server's answer for feedback reports, `null` when offline.
 */
data class SyncIssue(val kind: SyncIssueKind, val since: Instant, val code: String? = null) {
    fun encode(): String = "${kind.name}|${since.toEpochMilliseconds()}|${code.orEmpty()}"

    companion object {
        const val KEY = "sync_issue"

        fun decode(value: String?): SyncIssue? {
            val parts = value?.takeIf { it.isNotEmpty() }?.split('|', limit = 3) ?: return null
            if (parts.size < 2) return null
            val kind = SyncIssueKind.entries.firstOrNull { it.name == parts[0] } ?: return null
            val since = parts[1].toLongOrNull()?.let(Instant::fromEpochMilliseconds) ?: return null
            return SyncIssue(kind, since, parts.getOrNull(2)?.takeIf { it.isNotEmpty() })
        }

        /** The issue for [error] at [now]; the same kind as [previous] keeps its start time. */
        fun of(error: Throwable, now: Instant, previous: SyncIssue?): SyncIssue {
            val (kind, code) = classify(error)
            return SyncIssue(kind, if (previous?.kind == kind) previous.since else now, code)
        }

        fun classify(error: Throwable): Pair<SyncIssueKind, String?> {
            if (error !is ApiException) return SyncIssueKind.OFFLINE to null
            val code = error.status ?: "HTTP ${error.code}"
            val kind = when {
                error.isUnauthenticated -> SyncIssueKind.SIGNED_OUT
                error.isPermissionDenied -> SyncIssueKind.NO_ACCESS
                error.code == 429 || error.status == "RESOURCE_EXHAUSTED" -> SyncIssueKind.QUOTA
                else -> SyncIssueKind.SERVER
            }
            return kind to code
        }
    }
}

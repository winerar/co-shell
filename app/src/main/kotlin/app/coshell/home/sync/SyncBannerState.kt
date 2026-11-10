package app.coshell.home.sync

import app.coshell.core.data.sync.SyncIssueKind

/**
 * The banner over the total on «Обзор» when sync keeps failing (board 17А, feedback #102).
 * The title, text and button come from [kind]; the rest fills them in.
 *
 * @property pending local edits waiting to be sent.
 * @property lastSync «9:12» / «9 ноября 9:12», `null` — never synced.
 * @property code the server's answer, for the feedback report.
 * @property resumeAt local time the free Firebase quota resets («4:00»).
 */
data class SyncBannerState(
    val kind: SyncIssueKind,
    val pending: Int = 0,
    val lastSync: String? = null,
    val code: String? = null,
    val resumeAt: String? = null,
    val isBusy: Boolean = false,
)

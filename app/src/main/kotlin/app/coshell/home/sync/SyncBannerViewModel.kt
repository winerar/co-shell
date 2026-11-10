package app.coshell.home.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.family.FamilyService
import app.coshell.core.data.family.FamilyState
import app.coshell.core.data.sync.SyncEngine
import app.coshell.core.data.sync.SyncIssue
import app.coshell.core.data.sync.SyncIssueKind
import app.coshell.ui.format.formatClock
import app.coshell.ui.format.formatDayTime
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

/**
 * Feeds the sync banner on «Обзор» from Room ([SyncIssue] in `sync_state`, written by
 * [FamilyService.sync]); re-checks the time every [TICK_MS] so the banner appears after the grace.
 */
@HiltViewModel
class SyncBannerViewModel @Inject constructor(
    private val family: FamilyService,
    syncDao: SyncDao,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    private val dismissed = MutableStateFlow<SyncIssue?>(null)
    /** The issue behind the banner on screen: «Понятно» hides exactly it. */
    private var shown: SyncIssue? = null
    private val busy = MutableStateFlow(false)
    private val ticks = flow { while (true) { emit(Unit); delay(TICK_MS) } }
    private val local = combine(dismissed, busy, ticks) { d, b, _ -> d to b }

    val state: StateFlow<SyncBannerState?> = combine(
        syncDao.observeState(SyncIssue.KEY).map(SyncIssue::decode),
        syncDao.observePendingCount(),
        syncDao.observeState(SyncEngine.LAST_SYNC),
        family.state,
        local,
    ) { issue, pending, last, familyState, (hidden, isBusy) ->
        val now = clock.now()
        if (issue == null || !SyncBannerRules.shouldShow(issue, now, familyState is FamilyState.Joined, hidden)) return@combine null
        shown = issue
        SyncBannerState(
            kind = issue.kind,
            pending = pending,
            lastSync = last?.toLongOrNull()?.let { formatDayTime(Instant.fromEpochMilliseconds(it), now, timeZone) },
            code = issue.code,
            resumeAt = if (issue.kind == SyncIssueKind.QUOTA) formatClock(SyncBannerRules.quotaResetAt(now, timeZone)) else null,
            isBusy = isBusy,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** «Повторить»: one sync now; a failure updates the issue by itself. */
    fun retry() {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try { runCatching { family.sync() } } finally { busy.value = false }
        }
    }

    /** «Понятно» (quota): hide until the problem changes. */
    fun dismiss() {
        dismissed.value = shown
    }

    private companion object {
        const val TICK_MS = 30_000L
    }
}

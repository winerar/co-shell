package app.coshell.family.sync

import android.content.Context
import android.util.Log
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.family.FamilyService
import app.coshell.core.data.family.FamilyState
import app.coshell.core.data.sync.FamilyBell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Sync while the app is on screen: pull at once when it opens, then once a minute ([POLL_MS],
 * owner's choice 2026-11-11: an idle pull costs a Firestore read, 60 an hour per phone). The
 * [bell] stream makes the other phone's edits arrive within seconds; polling is only the fallback
 * if the bell is down. A local edit schedules a send (the worker, a second later) and restarts
 * the minute. Started from
 * [app.coshell.MainActivity] with `repeatOnLifecycle(STARTED)`, so it restarts on every return to
 * the app and stops in the background (the stream too).
 */
class ForegroundSync(
    private val context: Context,
    private val family: FamilyService,
    private val dao: SyncDao,
    private val bell: FamilyBell,
) {
    suspend fun run(scope: CoroutineScope) {
        family.load()
        val wake = Channel<Unit>(Channel.CONFLATED)
        scope.launch {
            dao.observePendingCount().distinctUntilChanged().filter { it > 0 }.collect {
                if (family.joined != null) {
                    SyncScheduler.syncSoon(context)
                    wake.trySend(Unit) // restart the minute from this edit
                }
            }
        }
        scope.launch { listenToBell { wake.trySend(Unit) } }
        family.refresh()
        val backoff = PollBackoff(POLL_MS, POLL_MS, fastIdleRuns = 0)
        pollForever(backoff, wake, onError = { Log.w(TAG, "Foreground sync failed: ${it.message}") }) {
            val result = family.sync()
            result != null && (result.pulled > 0 || result.pushed > 0)
        }
    }

    /** While in the family: pull on every ring of another member; a new member list re-registers. */
    private suspend fun listenToBell(onRing: () -> Unit) {
        family.state
            .map { (it as? FamilyState.Joined)?.family?.members?.map { m -> m.uid }?.toSet() }
            .distinctUntilChanged()
            .collectLatest { uids ->
                if (uids == null) return@collectLatest
                bell.rings(uids, onError = { Log.i(TAG, "Bell: ${it.message}") }).conflate().collect {
                    runCatching { family.sync() }.onFailure { Log.w(TAG, "Sync on ring failed: ${it.message}") }
                    onRing()
                }
            }
    }

    private companion object {
        const val TAG = "Sync"
        const val POLL_MS = 60_000L
    }
}

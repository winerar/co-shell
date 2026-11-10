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
 * Sync while the app is on screen: pull at once when it opens, then every [BASE_MS] while
 * something changes; after a minute of quiet the pause grows to [MAX_MS] (an idle pull costs one
 * Firestore read: 360 an hour per phone at a fixed 10 s, ≈ 60 at the slow pace). A local edit
 * schedules a send and brings back the fast pace. The [bell] stream makes the other phone's edits
 * arrive within seconds; polling is the fallback if the bell is down. Started from
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
                    wake.trySend(Unit) // the other phone may answer soon: poll fast again
                }
            }
        }
        scope.launch { listenToBell { wake.trySend(Unit) } }
        family.refresh()
        val backoff = PollBackoff(BASE_MS, MAX_MS, FAST_IDLE_RUNS)
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
        const val BASE_MS = 10_000L
        const val MAX_MS = 60_000L
        const val FAST_IDLE_RUNS = 6
    }
}

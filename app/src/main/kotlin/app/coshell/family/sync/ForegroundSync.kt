package app.coshell.family.sync

import android.content.Context
import android.util.Log
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.family.FamilyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Sync while the app is on screen: pull every [INTERVAL_MS] (an idle pull costs one Firestore
 * read) and schedule a send whenever local edits appear. Started from [app.coshell.MainActivity]
 * with `repeatOnLifecycle(STARTED)`, so it stops in the background.
 */
class ForegroundSync(
    private val context: Context,
    private val family: FamilyService,
    private val dao: SyncDao,
) {
    suspend fun run(scope: CoroutineScope) {
        family.load()
        scope.launch {
            dao.observePendingCount().distinctUntilChanged().filter { it > 0 }.collect {
                if (family.joined != null) SyncScheduler.syncSoon(context)
            }
        }
        family.refresh()
        while (true) {
            runCatching { family.sync() }.onFailure { Log.w("Sync", "Foreground sync failed: ${it.message}") }
            delay(INTERVAL_MS)
        }
    }

    private companion object {
        const val INTERVAL_MS = 60_000L
    }
}

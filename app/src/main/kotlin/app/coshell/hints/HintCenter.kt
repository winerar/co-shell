package app.coshell.hints

import app.coshell.core.domain.hints.HintId
import app.coshell.core.domain.hints.HintPlanner
import app.coshell.core.domain.hints.HintStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Screens ask «may I show this hint now?» through this; the app provides it via [LocalHintHost]. */
fun interface HintHost {
    suspend fun claim(id: HintId): Boolean
}

/**
 * Decides, with [HintPlanner], whether a hint may appear: hints on, not shown before, one per visit (= app process)
 * and one per day. A claimed hint counts as shown at once, so «✕» only closes it (`docs/agents/hints.md`).
 */
@Singleton
class HintCenter(private val store: HintStore, private val today: () -> Long) : HintHost {
    @Inject constructor(store: HintStore) : this(store, { System.currentTimeMillis() / MILLIS_PER_DAY })

    private val lock = Mutex()
    private var shownThisVisit = false

    override suspend fun claim(id: HintId): Boolean = lock.withLock {
        val day = today()
        val pick = HintPlanner.next(setOf(id), store.enabled(), store.shown(), store.lastShownDay(), day, shownThisVisit)
        if (pick == null) return false
        store.markShown(id.key)
        store.setLastShownDay(day)
        shownThisVisit = true
        true
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}

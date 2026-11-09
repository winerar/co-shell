package app.coshell.rates

import kotlinx.coroutines.flow.Flow

/** "Update now" on the rates screen; an interface so the ViewModel can be tested without WorkManager. */
interface RatesRefresher {
    /** Starts a one-off rates download right away (replaces one that is still running). */
    fun refreshNow()

    /** State of the latest one-off download; [RefreshStatus.Idle] if there was none. */
    fun observe(): Flow<RefreshStatus>
}

enum class RefreshStatus {
    Idle,
    Running,
    /** New latest rates were saved. */
    Updated,
    /** Today's rates were already stored; nothing to download. */
    AlreadyFresh,
    /** No source answered (no network or all sources down). */
    Failed,
}

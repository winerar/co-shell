package app.coshell.gate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.sync.VersionGate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Entry is closed until the update (#334, board `Fb334A`): the other phone runs a newer, incompatible
 * version ([VersionGate]). Looking stays open; saving, deleting and «+» answer with [onBlocked]
 * (a note with «Обновить») instead of writing.
 */
@Immutable
data class InputLock(val locked: Boolean = false, val onBlocked: () -> Unit = {}) {
    /** [action], or [onBlocked] while entry is closed. */
    fun guard(action: () -> Unit): () -> Unit = { if (locked) onBlocked() else action() }

    /** One-argument form of [guard]. */
    fun <T> guard1(action: (T) -> Unit): (T) -> Unit = { if (locked) onBlocked() else action(it) }

    /** Two-argument form of [guard]. */
    fun <A, B> guard2(action: (A, B) -> Unit): (A, B) -> Unit = { a, b -> if (locked) onBlocked() else action(a, b) }
}

/** Given by `CoShellNavHost` to every screen; previews and tests get the open default. */
val LocalInputLock = staticCompositionLocalOf { InputLock() }

/** Reads the gate from Room for the app shell. */
@HiltViewModel
class InputLockViewModel @Inject constructor(syncDao: SyncDao) : ViewModel() {
    val locked: StateFlow<Boolean> = syncDao.observeState(VersionGate.KEY)
        .map(VersionGate::isLocked)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}

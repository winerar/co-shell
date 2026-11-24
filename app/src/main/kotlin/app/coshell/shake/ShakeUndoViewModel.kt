package app.coshell.shake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.shake.ShakeIntent
import app.coshell.undo.SavedEntryUndo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/** What a shake does after a save (#325): read by `CoShellNavHost`; the undo runs in this scope, so it finishes if the screen changes. */
@HiltViewModel
class ShakeUndoViewModel @Inject constructor(private val saved: SavedEntryUndo) : ViewModel() {
    /** A new operation was saved: the one-time card about shaking waits for it. */
    val saves: SharedFlow<Unit> = saved.saves

    fun intent(): ShakeIntent = saved.intent()

    fun undoLast() {
        viewModelScope.launch { saved.undoLast() }
    }
}

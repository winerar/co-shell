package app.coshell.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlinx.coroutines.launch

/** Window of a `dialog<…>` destination that hosts an [EditSheetHost]: full screen, insets left to the sheet. */
val EditSheetDialogProperties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)

/**
 * An editor as a bottom sheet (design-questions §5): slides up over the current screen, closes with a
 * swipe down, a tap outside or the back gesture. Lives in a `dialog<…>` navigation destination
 * (so its ViewModel and arguments work as for a screen); [onClosed] pops that destination.
 * [content] gets `close` — slide the sheet down, then [onClosed]; pass it as the editor's `onDone`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSheetHost(onClosed: () -> Unit, content: @Composable (close: () -> Unit) -> Unit) {
    // The dialog window must not dim the screen too: the sheet draws its own scrim.
    val view = LocalView.current
    LaunchedEffect(view) { (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val once = remember { CloseOnce(onClosed) }
    val close: () -> Unit = { scope.launch { sheetState.hide() }.invokeOnCompletion { once.close() } }

    ModalBottomSheet(onDismissRequest = once::close, sheetState = sheetState) {
        content(close)
    }
}

/** Pops the destination once, whether the sheet was swiped away or closed after saving. */
private class CloseOnce(private val onClosed: () -> Unit) {
    private var closed = false

    fun close() {
        if (closed) return
        closed = true
        onClosed()
    }
}

package app.coshell.gate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.R as DesignR

/**
 * The app's [InputLock]. A blocked tap opens a short window — why, and «Обновить» ([onUpdate]). A window,
 * not a snackbar: the tap often comes from an editor sheet, and the shell's snackbar sits under it.
 */
@Composable
fun rememberInputLock(locked: Boolean, onUpdate: () -> Unit): InputLock {
    var noteShown by rememberSaveable { mutableStateOf(false) }
    if (noteShown && locked) {
        CoShellAlertDialog(
            title = stringResource(R.string.gate_banner_title_any),
            text = stringResource(R.string.gate_locked_note),
            confirmLabel = stringResource(R.string.gate_banner_update),
            onConfirm = {
                noteShown = false
                onUpdate()
            },
            onDismiss = { noteShown = false },
            dismissLabel = stringResource(R.string.gate_note_later),
            icon = DesignR.drawable.ic_lock,
        )
    }
    return remember(locked) { InputLock(locked) { noteShown = true } }
}

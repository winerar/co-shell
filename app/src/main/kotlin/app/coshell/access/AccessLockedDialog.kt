package app.coshell.access

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.R as DesignR

/**
 * «Доступ закрыт» (#666, board `AccessLocked`): the code was revoked. The data stays and can be saved; entry is closed
 * (`gate/InputLock`). Shown on opening and after a blocked tap on «+» and the like.
 */
@Composable
fun AccessLockedDialog(onEnterCode: () -> Unit, onBackup: () -> Unit, onDismiss: () -> Unit) {
    CoShellAlertDialog(
        title = stringResource(R.string.access_locked_title),
        text = stringResource(R.string.access_locked_text),
        confirmLabel = stringResource(R.string.access_locked_enter),
        onConfirm = onEnterCode,
        onDismiss = onDismiss,
        dismissLabel = stringResource(R.string.access_locked_backup),
        onDismissClick = onBackup,
        icon = DesignR.drawable.ic_lock,
    )
}

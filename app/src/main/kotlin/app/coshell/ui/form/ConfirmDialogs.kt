package app.coshell.ui.form

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAlertDialog

/** Generic "are you sure?" dialog: [confirmLabel] runs [onConfirm], "Cancel" runs [onDismiss]. */
@Composable
fun ConfirmDialog(
    @StringRes title: Int,
    @StringRes text: Int,
    @StringRes confirmLabel: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    CoShellAlertDialog(
        title = stringResource(title),
        text = stringResource(text),
        confirmLabel = stringResource(confirmLabel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        dismissLabel = stringResource(R.string.action_cancel),
        destructive = destructive,
    )
}

/** "Delete X?" confirmation shared by all editors; «Удалить» is red. */
@Composable
fun ConfirmDeleteDialog(@StringRes title: Int, @StringRes text: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) =
    ConfirmDialog(title, text, R.string.action_delete, onConfirm, onDismiss, destructive = true)

/** Delete was refused because the item is in use; offers archiving instead. */
@Composable
fun DeleteBlockedDialog(@StringRes title: Int, @StringRes text: Int, onArchive: () -> Unit, onDismiss: () -> Unit) =
    ConfirmDialog(
        title = title,
        text = text,
        confirmLabel = R.string.action_archive,
        onConfirm = {
            onDismiss()
            onArchive()
        },
        onDismiss = onDismiss,
    )

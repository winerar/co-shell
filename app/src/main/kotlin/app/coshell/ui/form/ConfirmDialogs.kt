package app.coshell.ui.form

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellTextButton

/** Generic "are you sure?" dialog: [confirmLabel] runs [onConfirm], "Cancel" runs [onDismiss]. */
@Composable
fun ConfirmDialog(
    @StringRes title: Int,
    @StringRes text: Int,
    @StringRes confirmLabel: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(text)) },
        confirmButton = { CoShellTextButton(onClick = onConfirm) { Text(stringResource(confirmLabel)) } },
        dismissButton = { CoShellTextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** "Delete X?" confirmation shared by all editors. */
@Composable
fun ConfirmDeleteDialog(@StringRes title: Int, @StringRes text: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) =
    ConfirmDialog(title, text, R.string.action_delete, onConfirm, onDismiss)

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

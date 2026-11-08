package app.coshell.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R

/**
 * Bottom of an editor: "Save", and for an existing item "Archive/Unarchive" and "Delete".
 * Delete only asks for confirmation — see [ConfirmDeleteDialog].
 */
@Composable
fun EditFooter(
    isNew: Boolean,
    archived: Boolean,
    onSave: () -> Unit,
    onToggleArchived: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.action_save))
    }
    if (isNew) return
    OutlinedButton(onClick = onToggleArchived, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(if (archived) R.string.action_unarchive else R.string.action_archive))
    }
    DeleteButton(onDeleteClick)
}

/** Red "Delete" text button across the form. */
@Composable
fun DeleteButton(onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
    }
}

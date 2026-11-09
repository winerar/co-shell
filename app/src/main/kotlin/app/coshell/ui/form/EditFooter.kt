package app.coshell.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.components.CoShellTextButton

/**
 * End of an editor's form for an existing item: "Archive/Unarchive" and "Delete" ("Save" is pinned
 * at the bottom of the sheet — `EditSheetLayout`). Delete only asks for confirmation — see [ConfirmDeleteDialog].
 */
@Composable
fun EditFooter(
    isNew: Boolean,
    archived: Boolean,
    onToggleArchived: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    if (isNew) return
    CoShellOutlinedButton(onClick = onToggleArchived, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(if (archived) R.string.action_unarchive else R.string.action_archive))
    }
    DeleteButton(onDeleteClick)
}

/** Red "Delete" text button across the form. */
@Composable
fun DeleteButton(onClick: () -> Unit) {
    CoShellTextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
    }
}

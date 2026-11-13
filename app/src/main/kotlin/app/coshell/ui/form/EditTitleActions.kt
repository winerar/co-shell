package app.coshell.ui.form

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.R as DesignR

/**
 * «В архив» and «Удалить» as icons beside an editor's title (#290, board `Fb290FA`): the form keeps its room for
 * the item itself. Nothing for a new item. Delete asks first — see [ConfirmDeleteDialog].
 */
@Composable
fun EditTitleActions(isNew: Boolean, archived: Boolean, onToggleArchived: () -> Unit, onDeleteClick: () -> Unit) {
    if (isNew) return
    Row {
        CoShellIconButton(onClick = onToggleArchived) {
            Icon(
                painterResource(if (archived) DesignR.drawable.ic_unarchive else DesignR.drawable.ic_archive),
                contentDescription = stringResource(if (archived) R.string.action_unarchive else R.string.action_archive),
            )
        }
        CoShellIconButton(onClick = onDeleteClick) {
            Icon(
                painterResource(DesignR.drawable.ic_delete),
                contentDescription = stringResource(R.string.action_delete),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

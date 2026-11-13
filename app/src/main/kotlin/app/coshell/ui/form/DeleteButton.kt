package app.coshell.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellTextButton

/**
 * Red «Удалить» text button across a form (template editor). Editors redone for #290 put it beside the title —
 * [EditTitleActions]. Delete only asks for confirmation — see [ConfirmDeleteDialog].
 */
@Composable
fun DeleteButton(onClick: () -> Unit) {
    CoShellTextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
    }
}

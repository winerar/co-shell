package app.coshell.ui.sheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellSheetLayout

/** Editor inside a `CoShellSheet`: title (with [titleAction] — e.g. `EditTitleActions`), form and «Сохранить» pinned at the bottom (`CoShellSheetLayout`). */
@Composable
fun EditSheetLayout(
    title: String,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveEnabled: Boolean = true,
    showSave: Boolean = true,
    titleAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) = CoShellSheetLayout(
    title = title,
    actionLabel = stringResource(R.string.action_save),
    onAction = onSave,
    modifier = modifier,
    actionEnabled = saveEnabled,
    showAction = showSave,
    titleAction = titleAction,
    content = content,
)

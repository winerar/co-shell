package app.coshell.ui.sheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellSheet
import app.coshell.core.designsystem.components.CoShellSheetDialogProperties
import app.coshell.core.designsystem.components.CoShellSheetLayout

/**
 * A small sheet over a form (#290: a detail opens its own sheet instead of growing the form): [title], the content
 * and «Готово». Lives in its own `Dialog`, so it can open from any screen or sheet; [content] gets `close`.
 */
@Composable
fun FormSheet(title: String, onClosed: () -> Unit, content: @Composable ColumnScope.(close: () -> Unit) -> Unit) {
    Dialog(onDismissRequest = onClosed, properties = CoShellSheetDialogProperties) {
        CoShellSheet(onClosed = onClosed) { close ->
            CoShellSheetLayout(title = title, actionLabel = stringResource(R.string.keypad_done), onAction = close) { content(close) }
        }
    }
}

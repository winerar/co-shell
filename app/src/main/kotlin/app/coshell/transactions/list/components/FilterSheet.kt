package app.coshell.transactions.list.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellSheet
import app.coshell.core.designsystem.components.CoShellSheetDialogProperties
import app.coshell.core.designsystem.components.CoShellSheetLayout
import app.coshell.core.designsystem.components.CoShellTextButton

/**
 * A filter sheet of «Операции» (variant А): choices apply at once — the list and [count] follow while the sheet
 * is open; «Показать N операций» only closes it, «Сбросить» clears this sheet's filter.
 */
@Composable
internal fun FilterSheet(
    title: String,
    count: Int?,
    onReset: (() -> Unit)?,
    onClosed: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onClosed, properties = CoShellSheetDialogProperties) {
        CoShellSheet(onClosed = onClosed) { close ->
            CoShellSheetLayout(
                title = title,
                actionLabel = count?.let { pluralStringResource(R.plurals.filter_show_count, it, it) } ?: stringResource(R.string.filter_show),
                onAction = close,
                titleAction = onReset?.let { reset ->
                    { CoShellTextButton(onClick = reset) { Text(stringResource(R.string.filter_sheet_reset)) } }
                },
                content = content,
            )
        }
    }
}

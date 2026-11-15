package app.coshell.ui.sheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellModalSheet

/**
 * Every form or choice over a screen (#345, variant В): [CoShellModalSheet] with the app's «Закрыть» on the ×.
 * [onAction] gets `close`; a form whose screen drops it after saving just calls its save.
 */
@Composable
fun ModalSheet(
    title: String,
    onClosed: () -> Unit,
    subtitle: String? = null,
    actionLabel: String? = null,
    actionEnabled: Boolean = true,
    onAction: (close: () -> Unit) -> Unit = { it() },
    content: @Composable ColumnScope.(close: () -> Unit) -> Unit,
) = CoShellModalSheet(
    title = title,
    closeLabel = stringResource(R.string.action_close),
    onClosed = onClosed,
    subtitle = subtitle,
    actionLabel = actionLabel,
    actionEnabled = actionEnabled,
    onAction = onAction,
    content = content,
)

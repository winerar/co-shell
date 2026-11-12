package app.coshell.ui.form

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellDialog
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.R as DesignR

/** One line of a [ChoiceDialog]. */
data class ChoiceOption<T>(val value: T, val label: String, val subtitle: String? = null)

/** A list to pick one of [options]; a tap picks and closes, the selected one carries a check; [hint] — a line above. */
@Composable
fun <T> ChoiceDialog(
    title: String,
    options: List<ChoiceOption<T>>,
    selected: T?,
    onPick: (T) -> Unit,
    onDismiss: () -> Unit,
    hint: String? = null,
) {
    CoShellDialog(title = title, confirmLabel = stringResource(R.string.action_cancel), onConfirm = onDismiss, onDismiss = onDismiss) {
        hint?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        options.forEach { option ->
            CoShellListRow(
                title = option.label,
                subtitle = option.subtitle,
                trailing = if (option.value == selected) {
                    { Icon(painterResource(DesignR.drawable.ic_check), contentDescription = null) }
                } else {
                    null
                },
                onClick = { onPick(option.value) },
            )
        }
    }
}

package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.tokens.Spacing

// Dialogs (design-system.md §«Компоненты»): 28 dp corners (shapes.extraLarge), text buttons with the
// tactile press. For short questions and small forms only — editors are bottom sheets.

/**
 * Dialog with a form or long text: [content] scrolls and stacks with Spacing.m gaps. [dismissLabel] = null
 * → a single button. [destructive] paints the confirm label red («Удалить»).
 */
@Composable
fun CoShellDialog(
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String? = null,
    onDismissClick: () -> Unit = onDismiss,
    confirmEnabled: Boolean = true,
    destructive: Boolean = false,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                content()
            }
        },
        confirmButton = {
            CoShellTextButton(onClick = onConfirm, enabled = confirmEnabled) {
                if (destructive) Text(confirmLabel, color = MaterialTheme.colorScheme.error) else Text(confirmLabel)
            }
        },
        dismissButton = dismissLabel?.let { label -> { CoShellTextButton(onClick = onDismissClick) { Text(label) } } },
    )
}

/** Short question: [text] and two buttons («Удалить» / «Отмена»), or one when [dismissLabel] is null. */
@Composable
fun CoShellAlertDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String? = null,
    onDismissClick: () -> Unit = onDismiss,
    destructive: Boolean = false,
) = CoShellDialog(
    title = title,
    confirmLabel = confirmLabel,
    onConfirm = onConfirm,
    onDismiss = onDismiss,
    dismissLabel = dismissLabel,
    onDismissClick = onDismissClick,
    destructive = destructive,
) { Text(text) }

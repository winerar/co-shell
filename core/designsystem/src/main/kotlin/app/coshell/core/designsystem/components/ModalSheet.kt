package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.tokens.Spacing

// Forms and choices over a screen (#345, variant В): a bottom sheet in its own window instead of a dialog —
// the action is under the thumb, numbers are typed on our keypad. Questions stay dialogs ([CoShellAlertDialog]).

/**
 * A sheet opened from any screen while it is in composition: [title] with an optional [subtitle] and ×,
 * the [content] (scrolls when tall) and the main action ([actionLabel]) pinned across the width. No
 * [actionLabel] — no button (a choice closes on a tap). [onClosed] fires once after a swipe down, a tap
 * outside, «назад» or ×; [content] and [onAction] get `close` — slide down, then [onClosed].
 */
@Composable
fun CoShellModalSheet(
    title: String,
    closeLabel: String,
    onClosed: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    actionEnabled: Boolean = true,
    onAction: (close: () -> Unit) -> Unit = { it() },
    content: @Composable ColumnScope.(close: () -> Unit) -> Unit,
) {
    Dialog(onDismissRequest = onClosed, properties = CoShellSheetDialogProperties) {
        CoShellSheet(onClosed = onClosed) { close ->
            CoShellModalSheetLayout(title, closeLabel, close, modifier, subtitle, actionLabel, actionEnabled, { onAction(close) }) { content(close) }
        }
    }
}

/** The inside of [CoShellModalSheet] without its window — for the catalog and screenshot references. */
@Composable
fun CoShellModalSheetLayout(
    title: String,
    closeLabel: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    actionEnabled: Boolean = true,
    onAction: () -> Unit = onClose,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.imePadding()) {
        Row(Modifier.fillMaxWidth().padding(start = Spacing.l + Spacing.xs, end = Spacing.s), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f).padding(top = Spacing.s)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.xs),
                    )
                }
            }
            CoShellIconButton(onClick = onClose) {
                Icon(painterResource(R.drawable.ic_close), contentDescription = closeLabel, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.l + Spacing.xs, vertical = Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
            content = content,
        )
        actionLabel?.let {
            CoShellButton(
                onClick = onAction,
                enabled = actionEnabled,
                tactile = Tactile.Primary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.m),
            ) { Text(it) }
        }
    }
}

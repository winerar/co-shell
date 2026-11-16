package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

// Questions (#345, variant В): a compact card in the middle of the screen — «стоп, подтвердите». Forms and
// choices are never dialogs: they are bottom sheets ([CoShellModalSheet]).

/**
 * Short question: icon, [title] as the question, [text] — what happens, and two pills «Отмена · [confirmLabel]»
 * (one when [dismissLabel] is null). [destructive] paints the icon and the confirm pill red and defaults the
 * icon to the bin. A tap outside or «назад» run [onDismiss].
 */
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
    @DrawableRes icon: Int? = if (destructive) R.drawable.ic_delete else null,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CoShellConfirmCard(
            title = title,
            text = text,
            confirmLabel = confirmLabel,
            onConfirm = onConfirm,
            dismissLabel = dismissLabel,
            onDismissClick = onDismissClick,
            destructive = destructive,
            icon = icon,
            modifier = Modifier.padding(horizontal = Spacing.l + Spacing.xs),
        )
    }
}

/** The card of [CoShellAlertDialog] without its window — for the catalog and screenshot references. */
@Composable
fun CoShellConfirmCard(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String? = null,
    onDismissClick: () -> Unit = {},
    destructive: Boolean = false,
    @DrawableRes icon: Int? = if (destructive) R.drawable.ic_delete else null,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.surfaceContainerHigh,
        contentColor = colors.onSurface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(start = Spacing.l + Spacing.xs, end = Spacing.l + Spacing.xs, top = Spacing.xl, bottom = Spacing.l + Spacing.xs)) {
            icon?.let {
                val (bg, ink) = if (destructive) colors.errorContainer to colors.onErrorContainer else colors.secondaryContainer to colors.onSecondaryContainer
                Box(Modifier.size(Sizes.pillHeight).background(bg, MaterialTheme.shapes.large), contentAlignment = Alignment.Center) {
                    Icon(painterResource(it), contentDescription = null, tint = ink)
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = if (icon != null) Spacing.m + Spacing.xxs else 0.dp),
            )
            if (text.isNotEmpty()) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.s),
                )
            }
            // Side by side while each button is wide enough for its words; on a narrow screen or with a big font —
            // one under the other (the main action first), so no label breaks into letters.
            BoxWithConstraints(Modifier.fillMaxWidth().padding(top = Spacing.l + Spacing.xs)) {
                val gap = Spacing.s + Spacing.xxs
                val stacked = dismissLabel != null &&
                    (maxWidth - gap) / 2 < BUTTON_MIN_WIDTH * LocalDensity.current.fontScale.coerceAtLeast(1f)
                val confirm: @Composable (Modifier) -> Unit = { m ->
                    CoShellButton(
                        onClick = onConfirm,
                        tactile = Tactile.Primary,
                        colors = if (destructive) {
                            ButtonDefaults.buttonColors(containerColor = colors.errorContainer, contentColor = colors.onErrorContainer)
                        } else {
                            coShellButtonColors()
                        },
                        modifier = m,
                    ) { Text(confirmLabel) }
                }
                val dismiss: @Composable (Modifier) -> Unit = { m ->
                    dismissLabel?.let { label -> CoShellOutlinedButton(onClick = onDismissClick, modifier = m) { Text(label) } }
                }
                if (stacked) {
                    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                        confirm(Modifier.fillMaxWidth().heightIn(min = Sizes.button))
                        dismiss(Modifier.fillMaxWidth().heightIn(min = Sizes.button))
                    }
                } else {
                    // Equal height of both buttons, growing with a long label instead of cutting it.
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(gap)) {
                        dismiss(Modifier.weight(1f).fillMaxHeight().heightIn(min = Sizes.button))
                        confirm(Modifier.weight(1f).fillMaxHeight().heightIn(min = Sizes.button))
                    }
                }
            }
        }
    }
}

/** Each of two dialog buttons needs at least this (× the font scale) to keep its words whole. */
private val BUTTON_MIN_WIDTH = 140.dp

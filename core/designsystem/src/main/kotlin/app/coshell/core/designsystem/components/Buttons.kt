package app.coshell.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.motion.smoothResize
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactile
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.PillShape

// Buttons of the app: Material 3 buttons with the [Tactile] motion instead of the ripple.
// Screens use these, never the Material ones (design-system.md §«Движение»).

/**
 * Filled pill button. [tactile] — `Primary` for the main action of a screen («Сохранить»).
 * Neon Mint fill with dark ink in both themes, like the «+» (owner's choice 2026-11-09 — no deep teal/green).
 */
@Composable
fun CoShellButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tactile: Tactile = Tactile.Control,
    colors: ButtonColors = coShellButtonColors(),
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        Button(
            onClick = onClick,
            modifier = modifier.tactile(source, tactile, PillShape, enabled).smoothResize(),
            enabled = enabled,
            colors = colors,
            interactionSource = source,
            content = content,
        )
    }
}

/** Colours of [CoShellButton]: the Neon Mint fill with dark ink on it. */
@Composable
fun coShellButtonColors(): ButtonColors {
    val colors = MaterialTheme.colorScheme
    return ButtonDefaults.buttonColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer)
}

/** Outlined pill button — secondary actions. [color] tints the label and the border (the family's violet). */
@Composable
fun CoShellOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.tactile(source, Tactile.Control, PillShape, enabled).smoothResize(),
            enabled = enabled,
            colors = color?.let { ButtonDefaults.outlinedButtonColors(contentColor = it) } ?: ButtonDefaults.outlinedButtonColors(),
            border = color?.let { BorderStroke(1.dp, it.copy(alpha = if (enabled) OUTLINE_TINT_ALPHA else DISABLED_ALPHA)) }
                ?: ButtonDefaults.outlinedButtonBorder(enabled),
            interactionSource = source,
            content = content,
        )
    }
}

private const val OUTLINE_TINT_ALPHA = 0.4f
private const val DISABLED_ALPHA = 0.12f

/** Text button — dialogs, quiet actions. */
@Composable
fun CoShellTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = accentInk(),
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        TextButton(
            onClick = onClick,
            modifier = modifier.tactile(source, Tactile.Control, PillShape, enabled).smoothResize(),
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
            interactionSource = source,
            content = content,
        )
    }
}

/** Round icon button. */
@Composable
fun CoShellIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        IconButton(
            onClick = onClick,
            modifier = modifier.tactile(source, Tactile.Control, PillShape, enabled).smoothResize(),
            enabled = enabled,
            colors = colors,
            interactionSource = source,
            content = content,
        )
    }
}

/** Floating action button; the bottom bar's "+" uses `Tactile.Primary`. */
@Composable
fun CoShellFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    tactile: Tactile = Tactile.Control,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        FloatingActionButton(
            onClick = onClick,
            // The fill flows into the sheet the «+» opens (issue #63).
            modifier = modifier.tactile(source, tactile, shape, originColor = containerColor),
            shape = shape,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = elevation,
            interactionSource = source,
            content = content,
        )
    }
}

/** Pill FAB with an icon and a word («+ Добавить») — the one add button of a list screen. */
@Composable
fun CoShellExtendedFab(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = contentColorFor(containerColor),
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        ExtendedFloatingActionButton(
            text = text,
            icon = icon,
            onClick = onClick,
            modifier = modifier.tactile(source, Tactile.Control, PillShape, originColor = containerColor),
            shape = PillShape,
            containerColor = containerColor,
            contentColor = contentColor,
            interactionSource = source,
        )
    }
}

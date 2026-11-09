package app.coshell.core.designsystem.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
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
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactile
import app.coshell.core.designsystem.theme.LocalDarkTheme
import app.coshell.core.designsystem.tokens.PillShape

// Buttons of the app: Material 3 buttons with the [Tactile] motion instead of the ripple.
// Screens use these, never the Material ones (design-system.md §«Движение»).

/**
 * Filled pill button. [tactile] — `Primary` for the main action of a screen («Сохранить»).
 * Light theme: neon mint fill with dark ink, like the «+» (owner's choice 2026-11-09 — the deep teal looked dull
 * next to the acid palette); dark theme: neon mint as is. Deep teal stays for links and small text.
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
            modifier = modifier.tactile(source, tactile, PillShape, enabled),
            enabled = enabled,
            colors = colors,
            interactionSource = source,
            content = content,
        )
    }
}

/** Colours of [CoShellButton]: the neon fill of the theme with readable text on it. */
@Composable
fun coShellButtonColors(): ButtonColors {
    val colors = MaterialTheme.colorScheme
    return if (LocalDarkTheme.current) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.buttonColors(containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer)
    }
}

/** Outlined pill button — secondary actions. */
@Composable
fun CoShellOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.tactile(source, Tactile.Control, PillShape, enabled),
            enabled = enabled,
            interactionSource = source,
            content = content,
        )
    }
}

/** Text button — dialogs, quiet actions. */
@Composable
fun CoShellTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        TextButton(
            onClick = onClick,
            modifier = modifier.tactile(source, Tactile.Control, PillShape, enabled),
            enabled = enabled,
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
            modifier = modifier.tactile(source, Tactile.Control, PillShape, enabled),
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
            modifier = modifier.tactile(source, tactile, shape),
            shape = shape,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = elevation,
            interactionSource = source,
            content = content,
        )
    }
}

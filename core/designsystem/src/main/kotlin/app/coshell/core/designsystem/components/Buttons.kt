package app.coshell.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.motion.smoothResize
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactile
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.theme.secondaryInk
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.tokens.Sizes

// Buttons of the app: Material 3 buttons with the [Tactile] motion instead of the ripple, all [Sizes.button] high.
// Screens use these, never the Material ones (design-system.md §«Движение»).

/** Filled tones of [CoShellButton] (board `CompNew` «Кнопки: оттенки»): the main neon one, the quiet violet one. */
enum class ButtonTone { Primary, Tonal }

/**
 * Filled pill button. [tactile] — `Primary` for the main action of a screen («Сохранить»).
 * Neon Mint fill with dark ink in both themes, like the «+» (owner's choice 2026-11-09 — no deep teal/green);
 * [ButtonTone.Tonal] — a secondary action that repeats in a list («Провести» in «Скоро»).
 */
@Composable
fun CoShellButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tactile: Tactile = Tactile.Control,
    tone: ButtonTone = ButtonTone.Primary,
    colors: ButtonColors = if (tone == ButtonTone.Tonal) tonalButtonColors() else coShellButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = Sizes.button).tactile(source, tactile, PillShape, enabled).smoothResize(),
            enabled = enabled,
            colors = colors,
            border = fillEdge(colors.containerColor),
            contentPadding = contentPadding,
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

/** Colours of [CoShellButton] in a status colour (a warning card's «Обновить», #334): [container] fill, [content] ink. */
@Composable
fun coShellButtonColors(container: Color, content: Color): ButtonColors =
    ButtonDefaults.buttonColors(containerColor = container, contentColor = content)

@Composable
private fun tonalButtonColors(): ButtonColors {
    val colors = MaterialTheme.colorScheme
    return ButtonDefaults.buttonColors(containerColor = colors.secondaryContainer, contentColor = colors.onSecondaryContainer)
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
            modifier = modifier.heightIn(min = Sizes.button).tactile(source, Tactile.Control, PillShape, enabled).smoothResize(),
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
    contentColor: Color = secondaryInk(),
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        TextButton(
            onClick = onClick,
            modifier = modifier.heightIn(min = Sizes.button).tactile(source, Tactile.Control, PillShape, enabled).smoothResize(),
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
            interactionSource = source,
            content = content,
        )
    }
}

/**
 * Icon button: a 48 dp circle on the filled track, like every button 48 high (board Style6, owner 2026-11-15).
 * Over a coloured surface pass [coShellIconButtonColors] with that surface's own container.
 */
@Composable
fun CoShellIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = coShellIconButtonColors(),
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        IconButton(
            onClick = onClick,
            modifier = modifier.size(Sizes.button).tactile(source, Tactile.Control, PillShape, enabled),
            enabled = enabled,
            colors = colors,
            interactionSource = source,
            content = content,
        )
    }
}

/** Colours of [CoShellIconButton]: the track of choice controls under the icon in ink. */
@Composable
fun coShellIconButtonColors(container: Color = MaterialTheme.colorScheme.surfaceContainerHigh): IconButtonColors =
    IconButtonDefaults.iconButtonColors(containerColor = container, contentColor = MaterialTheme.colorScheme.onSurface)

/** Floating action button; the bottom bar's "+" uses `Tactile.Primary`. */
@Composable
fun CoShellFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(containerColor),
    tactile: Tactile = Tactile.Control,
    content: @Composable () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        FloatingActionButton(
            onClick = onClick,
            // The fill flows into the sheet the «+» opens (issue #63).
            modifier = modifier.tactile(source, tactile, shape, originColor = containerColor).fillEdge(containerColor, shape),
            shape = shape,
            containerColor = containerColor,
            contentColor = contentColor,
            // No shadows in the design (owner 2026-11-16, #477): depth is fill and edge only.
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
            interactionSource = source,
            content = content,
        )
    }
}

/**
 * The one add button of a list screen (owner's choice 2026-11-16, #408): a round [Sizes.fab] «+» without text, bottom
 * right ([label] is only its accessible name: «Новый счёт»). A screen puts it in `Scaffold(floatingActionButton = …)` and
 * leaves [Sizes.fabClearance] under the list. Board `Add408Acc`; the style of every future add button.
 */
@Composable
fun CoShellAddFab(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    CoShellFab(
        onClick = onClick,
        modifier = modifier.size(Sizes.fab),
        shape = CircleShape,
        containerColor = colors.primary,
        contentColor = colors.onPrimary,
    ) { Icon(painterResource(R.drawable.ic_add), label, Modifier.size(Sizes.fabIcon)) }
}

package app.coshell.core.designsystem.motion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role

/**
 * `clickable` for our own elements (rows, swatches, badges): [tactile] feedback instead of the
 * Material ripple. Put it first in the chain, before background/padding.
 */
fun Modifier.tactileClickable(
    tactile: Tactile = Tactile.Row,
    shape: Shape? = null,
    enabled: Boolean = true,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    tactile(source, tactile, shape, enabled)
        .clickable(interactionSource = source, indication = null, enabled = enabled, role = role, onClick = onClick)
}

/** `toggleable` with [tactile] feedback — e.g. a settings row with a switch. */
fun Modifier.tactileToggleable(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    tactile: Tactile = Tactile.Row,
    role: Role? = Role.Switch,
): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    tactile(source, tactile)
        .toggleable(value = value, interactionSource = source, indication = null, role = role, onValueChange = onValueChange)
}

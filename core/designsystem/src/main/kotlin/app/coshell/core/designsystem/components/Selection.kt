package app.coshell.core.designsystem.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.smoothResize
import app.coshell.core.designsystem.motion.tactile
import app.coshell.core.designsystem.tokens.PillShape

// Choice controls with the [Tactile] press and the selection "pop".

/** Chip (pill) for choosing an account, category, date… Pops when it becomes selected. */
@Composable
fun CoShellFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = label,
            modifier = modifier.tactile(source, Tactile.Control, PillShape, enabled).selectionPop(selected).smoothResize(),
            enabled = enabled,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            interactionSource = source,
        )
    }
}

/** Switch. `onCheckedChange = null` when the whole row toggles (then the row is tactile). */
@Composable
fun CoShellSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier.tactile(source, Tactile.Control, PillShape, enabled).selectionPop(checked),
            enabled = enabled,
            interactionSource = source,
        )
    }
}

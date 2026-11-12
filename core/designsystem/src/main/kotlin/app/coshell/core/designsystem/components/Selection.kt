package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.R
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

/**
 * Check box of a multi-choice sheet row (#141, board `Fb141ACats`): 22 dp rounded square — empty frame when off,
 * neon with a check when on, neon with a dash when only some of a folder are chosen. Only shows: the row toggles.
 */
@Composable
fun CoShellCheckMark(state: ToggleableState, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(7.dp)
    val box = modifier.size(22.dp).selectionPop(state == ToggleableState.On)
    if (state == ToggleableState.Off) {
        Box(box.border(2.dp, colors.outline, shape))
        return
    }
    Box(box.background(colors.primary, shape), contentAlignment = Alignment.Center) {
        if (state == ToggleableState.On) {
            Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(16.dp))
        } else {
            Box(Modifier.size(width = 10.dp, height = 2.dp).background(colors.onPrimary))
        }
    }
}

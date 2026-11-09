package app.coshell.core.designsystem.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactile

/** Bottom bar tab: sinks under the finger like any control, its icon pops when the tab opens. */
@Composable
fun RowScope.CoShellNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    colors: NavigationBarItemColors = NavigationBarItemDefaults.colors(),
) {
    val source = remember { MutableInteractionSource() }
    NoRipple {
        NavigationBarItem(
            selected = selected,
            onClick = onClick,
            icon = { Box(Modifier.selectionPop(selected)) { icon() } },
            label = label,
            modifier = Modifier.tactile(source, Tactile.Control),
            colors = colors,
            interactionSource = source,
        )
    }
}

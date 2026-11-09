package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.theme.LocalDarkTheme

/** One tab of [CoShellBottomBar]. */
@Immutable
data class CoShellTab(@DrawableRes val icon: Int, val label: String, val selected: Boolean)

/**
 * Bottom bar (design-system.md §«Навигация и жесты»): tabs with the round «+» in the middle — the most
 * frequent action is always under the thumb. Active tab: outlined icon on a neon pill (bright mint in
 * both themes). [onTab] gets the tab's index in [tabs]; [addLabel] is the «+» for TalkBack.
 */
@Composable
fun CoShellBottomBar(tabs: List<CoShellTab>, onTab: (Int) -> Unit, addLabel: String, onAdd: () -> Unit) {
    val dark = LocalDarkTheme.current
    val scheme = MaterialTheme.colorScheme
    val tabColors = NavigationBarItemDefaults.colors(
        indicatorColor = if (dark) scheme.primary else scheme.primaryContainer,
        selectedIconColor = if (dark) scheme.onPrimary else scheme.onPrimaryContainer,
        selectedTextColor = scheme.onSurface,
    )
    NavigationBar {
        tabs.forEachIndexed { index, tab ->
            if (index == tabs.size / 2) {
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    CoShellFab(
                        onClick = onAdd,
                        shape = CircleShape,
                        tactile = Tactile.Primary,
                        containerColor = scheme.primary,
                        contentColor = scheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    ) {
                        Icon(painterResource(R.drawable.ic_add), addLabel)
                    }
                }
            }
            CoShellNavigationBarItem(
                colors = tabColors,
                selected = tab.selected,
                onClick = { onTab(index) },
                icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                label = { Text(tab.label) },
            )
        }
    }
}

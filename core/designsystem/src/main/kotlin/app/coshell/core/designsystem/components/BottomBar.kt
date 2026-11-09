package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.motion.ConfirmPulse
import app.coshell.core.designsystem.motion.MorphIcon
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.confirmProgress
import app.coshell.core.designsystem.theme.LocalDarkTheme
import app.coshell.core.designsystem.tokens.Motion

/** One tab of [CoShellBottomBar]. */
@Immutable
data class CoShellTab(@DrawableRes val icon: Int, val label: String, val selected: Boolean)

/**
 * Bottom bar (design-system.md §«Навигация и жесты»): tabs with the round «+» in the middle — the most
 * frequent action is always under the thumb. Active tab: outlined icon on a neon pill of the palette's
 * primary; the pill flows from tab to tab (`TabPill.kt`), the icon pops. [onTab] gets the tab's index in [tabs]; [addLabel] is the «+» for TalkBack.
 *
 * When [visible] turns false (an inner screen opened) the bar rides down off the screen and the space under
 * it closes with the same spring, so the screen above never jumps (design-refactor.md, Step 3).
 */
@Composable
fun CoShellBottomBar(
    tabs: List<CoShellTab>,
    onTab: (Int) -> Unit,
    addLabel: String,
    onAdd: () -> Unit,
    visible: Boolean = true,
    addConfirm: ConfirmPulse? = null,
) {
    AnimatedVisibility(visible = visible, enter = BarIn, exit = BarOut) {
        BottomBarContent(tabs, onTab, addLabel, onAdd, addConfirm)
    }
}

/**
 * The «+»: after a save ([addConfirm] fires) it turns into a check mark with a short warm flash of the
 * palette's third colour (design-refactor Step 4: «Сохранено» — тёплая вспышка), then back.
 */
@Composable
private fun AddButton(label: String, onAdd: () -> Unit, addConfirm: ConfirmPulse?) {
    val scheme = MaterialTheme.colorScheme
    val progress = confirmProgress(addConfirm)
    val p = progress.value.coerceIn(0f, 1f)
    CoShellFab(
        onClick = onAdd,
        shape = CircleShape,
        tactile = Tactile.Primary,
        containerColor = lerp(scheme.primary, scheme.tertiary, p),
        contentColor = lerp(scheme.onPrimary, scheme.onTertiary, p),
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
    ) {
        MorphIcon(
            progress = { progress.value },
            plus = { Icon(painterResource(R.drawable.ic_add), label) },
            check = { Icon(painterResource(R.drawable.ic_check), contentDescription = null) },
        )
    }
}

private val BarIn = slideInVertically(Motion.Placement) { it } + expandVertically(Motion.Resize, expandFrom = Alignment.Top)
private val BarOut = slideOutVertically(Motion.Placement) { it } + shrinkVertically(Motion.Resize, shrinkTowards = Alignment.Top)

@Composable
private fun BottomBarContent(
    tabs: List<CoShellTab>,
    onTab: (Int) -> Unit,
    addLabel: String,
    onAdd: () -> Unit,
    addConfirm: ConfirmPulse?,
) {
    val dark = LocalDarkTheme.current
    val scheme = MaterialTheme.colorScheme
    // One pill flows between tabs (TabPill.kt), so the per-tab Material indicator is transparent.
    val pillColor = if (dark) scheme.primary else scheme.primaryContainer
    val pill = rememberTabPillState()
    AnimateTabPill(pill, tabs.indexOfFirst { it.selected })
    val tabColors = NavigationBarItemDefaults.colors(
        indicatorColor = Color.Transparent,
        selectedIconColor = if (dark) scheme.onPrimary else scheme.onPrimaryContainer,
        selectedTextColor = scheme.onSurface,
    )
    NavigationBar(Modifier.tabPill(pill, pillColor)) {
        tabs.forEachIndexed { index, tab ->
            if (index == tabs.size / 2) {
                // Never fillMaxHeight here: the Scaffold measures the bar with the whole screen as
                // its max height, so the bar would take the screen and leave screens 0 px tall.
                Box(Modifier.weight(1f).align(Alignment.CenterVertically), contentAlignment = Alignment.Center) {
                    AddButton(addLabel, onAdd, addConfirm)
                }
            }
            CoShellNavigationBarItem(
                colors = tabColors,
                selected = tab.selected,
                onClick = { onTab(index) },
                icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                iconModifier = Modifier.tabPillAnchor(pill, index),
                label = { Text(tab.label) },
            )
        }
    }
}
